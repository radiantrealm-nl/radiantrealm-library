package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.*;
import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.net.io.SelectorEngine;
import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public abstract class WebsocketEngine extends SelectorEngine implements HttpHandler {
    protected final Queue<HttpConnection> pendingUpgrades = new ConcurrentLinkedQueue<>();
    protected final Map<String, WebsocketSession> sessionMap = new ConcurrentHashMap<>();
    protected final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();
    protected final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    protected final WebsocketConfiguration configuration;

    public WebsocketEngine(WebsocketConfiguration configuration) throws IOException {
        super(configuration.threadPoolSize());

        this.configuration = configuration;
    }

    @Override
    public void start() {
        super.start();

        scheduledExecutorService.scheduleWithFixedDelay(
                this::checkSessionTimeout,
                configuration.checkTimeoutMillis(),
                configuration.checkTimeoutMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void handle(HttpExchange exchange) {
        HttpResponse response = WebsocketHandshake.perform(exchange.request());

        HttpConnection connection = exchange.connection();
        connection.key.cancel();

        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        connection.outboundBuffer.add(buffer);

        pendingUpgrades.add(connection);
        selector.wakeup();
    }

    @Override
    protected void processKeyActions() {
        while (!pendingUpgrades.isEmpty()) {
            HttpConnection connection = pendingUpgrades.poll();

            try {
                SelectionKey key = connection.channel.register(selector, SelectionKey.OP_READ);
                WebsocketSession session = WebsocketSession.upgrade(
                        connection,
                        this,
                        key
                );

                key.attach(session);
                session.enableInterestOp(InterestOp.OP_WRITE);

                executorService.submit(() -> {
                    try {
                        onOpen(session);
                    } catch (WebsocketException e) {
                        try (session) {
                            sendFrame(session, e.frame);
                        }
                    } catch (RuntimeException e) {
                        try (session) {
                            sendFrame(session, WebsocketExitCode.INVALID_PAYLOAD_DATA.generateFrame());
                        }
                    }
                });
            } catch (IOException e) {
                logger.error("Exception while upgrading connection", e);

                try {
                    connection.close(true);
                } catch (IOException ignored) {}
            }
        }

        super.processKeyActions();
    }

    @Override
    protected void handleRead(SelectionKey key) {
        if (key.attachment() instanceof WebsocketSession session) {
            ByteBuffer buffer = ByteBuffer.allocate(configuration.incomingBufferSize());

            try {
                int written = session.channel.read(buffer);

                if (written == -1) {
                    session.close(true);

                    if (sessionMap.containsKey(session.sessionID)) {
                        executorService.submit(() -> onClose(session));
                    }
                    return;
                }

                if (written == 0) {
                    return;
                }
            } catch (IOException e) {
                logger.error("Exception while reading channel", e);
                return;
            }

            session.addInboundBuffer(buffer.flip());
            executorService.submit(() -> {
                WebsocketFrame frame = session.aggregatedWebsocketFrame.parse();

                if (frame == null) {
                    return;
                }

                try {
                    onFrame(session, frame);
                } catch (WebsocketException e) {
                    try (session) {
                        sendFrame(session, e.frame);
                    }
                } catch (RuntimeException e) {
                    try (session) {
                        sendFrame(session, WebsocketExitCode.INVALID_PAYLOAD_DATA.generateFrame());
                    }
                }
            });
        }
    }

    @Override
    protected void handleWrite(SelectionKey key) {
        if (key.attachment() instanceof WebsocketSession session) {
            VirtualByteBuffer outboundBuffer = session.outboundBuffer;

            try {
                synchronized (outboundBuffer) {
                    while (!outboundBuffer.isEmpty()) {
                        ByteBuffer buffer = ByteBuffer.wrap(outboundBuffer.peek(configuration.outgoingBufferSize()));
                        int written = session.channel.write(buffer);
                        outboundBuffer.poll(written);

                        if (buffer.hasRemaining()) {
                            return;
                        }
                    }

                    if (outboundBuffer.isEmpty()) {
                        session.disableInterestOp(InterestOp.OP_WRITE);
                        session.wakeup();

                        if (session.awaitClosing.get()) {
                            session.close(true);
                            executorService.submit(() -> onClose(session));
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Exception while writing channel", e);
            }
        }
    }

    protected void onMessage(WebsocketSession session, String message) {}
    protected void onBinary(WebsocketSession session, byte[] bytes) {}

    protected void sendFrame(WebsocketSession session, WebsocketFrame frame) {
        ByteBuffer buffer = ByteBuffer.wrap(frame.getBytes());
        session.addOutboundBuffer(buffer);
        session.enableInterestOp(InterestOp.OP_WRITE);
        session.wakeup();
    }

    protected void sendMessage(WebsocketSession session, String message) {
        sendFrame(session, new WebsocketFrame(
                WebsocketOperatorCode.TEXT,
                true,
                message.getBytes(StandardCharsets.UTF_8)
        ));
    }

    protected void sendBinary(WebsocketSession session, byte[] bytes) {
        sendFrame(session, new WebsocketFrame(
                WebsocketOperatorCode.BINARY,
                true,
                bytes
        ));
    }

    protected void onOpen(WebsocketSession session) {
        sessionMap.put(session.sessionID, session);
    }

    protected void onClose(WebsocketSession session) {
        sessionMap.remove(session.sessionID);
        cancelScheduledFuture(session);
    }

    protected void onFrame(WebsocketSession session, WebsocketFrame frame) {
        session.lastActivityMillis.set(System.currentTimeMillis());

        switch (frame.operatorCode()) {
            case CONTINUE, TEXT, BINARY -> glueFrames(session, frame);
            case CLOSE -> handleClose(session, frame);
            case PING -> handlePing(session);
            case PONG -> handlePong(session);
        }
    }

    protected void glueFrames(WebsocketSession session, WebsocketFrame frame) {
        Queue<WebsocketFrame> queue = session.fragmentedFrames;

        WebsocketFrame result = null;
        synchronized (queue) {
            if (frame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
                queue.add(frame);

                if (frame.finalMessage()) {
                    List<WebsocketFrame> list = new ArrayList<>(queue.size());
                    long payloadLength = 0;

                    for (WebsocketFrame websocketFrame : queue) {
                        if (list.isEmpty() && !websocketFrame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
                            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid frame fragmentation"));
                        } else if (!websocketFrame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
                            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid frame fragmentation"));
                        }

                        list.add(queue.poll());
                        payloadLength += websocketFrame.payload().length;

                        if (payloadLength > Integer.MAX_VALUE) {
                            throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG.generateFrame());
                        }

                        if (frame.finalMessage()) {
                            break;
                        }
                    }

                    byte[] resultBytes = new byte[(int) payloadLength];
                    int index = 0;

                    for (WebsocketFrame websocketFrame : list) {
                        byte[] payloadBytes = websocketFrame.payload();
                        System.arraycopy(payloadBytes, 0, resultBytes, index, payloadBytes.length);
                        index += payloadBytes.length;
                    }

                    result = new WebsocketFrame(
                            list.getFirst().operatorCode(),
                            true,
                            resultBytes
                    );
                }
            } else if (frame.finalMessage()) {
                result = frame;
            } else {
                queue.add(frame);
            }
        }

        if (result != null) {
            switch (result.operatorCode()) {
                case TEXT -> onMessage(session, new String(result.payload(), StandardCharsets.UTF_8));
                case BINARY -> onBinary(session, result.payload());
            }
        }
    }

    protected void handleClose(WebsocketSession session, WebsocketFrame frame) {
        byte[] bytes = frame.payload();

        if (bytes.length < 2 || bytes.length > 125) {
            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid close frame"));
        }

        WebsocketExitCode exitCode = WebsocketExitCode.valueOfCode((bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF);

        if (exitCode == null) {
            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid status code"));
        }

        if (!session.acknowledgedClosing.get()) {
            sendFrame(session, new WebsocketFrame(
                    WebsocketOperatorCode.CLOSE,
                    true,
                    frame.payload()
            ));
        } else {
            try (session) {
                onClose(session);
            }
        }
    }

    protected void handlePing(WebsocketSession session) {
        cancelScheduledFuture(session);
        sendFrame(session, new WebsocketFrame(
                WebsocketOperatorCode.PING,
                true,
                new byte[0]
        ));
    }

    protected void handlePong(WebsocketSession session) {
        cancelScheduledFuture(session);
    }

    protected void checkSessionTimeout() {
        long timestamp = System.currentTimeMillis();

        for (WebsocketSession session : sessionMap.values()) {
            if (scheduledFutureMap.containsKey(session.sessionID)) {
                continue;
            }

            if (timestamp - session.lastActivityMillis.get() > configuration.sessionTimeoutMillis()) {
                scheduledFutureMap.put(session.sessionID, scheduledExecutorService.schedule(() -> {
                    sendClosingHandshake(session, WebsocketExitCode.GOING_AWAY.generateFrame("PING timeout"));
                }, configuration.pingTimeoutMillis(), TimeUnit.MILLISECONDS));

                sendFrame(session, new WebsocketFrame(
                        WebsocketOperatorCode.PING,
                        true,
                        new byte[0]
                ));
            }
        }
    }

    protected void sendClosingHandshake(WebsocketSession session, WebsocketFrame frame) {
        scheduledFutureMap.put(session.sessionID, scheduledExecutorService.schedule(() -> {
            try (session) {
                onClose(session);
            }
        }, configuration.closeTimeoutMillis(), TimeUnit.MILLISECONDS));

        session.acknowledgedClosing.set(true);
        sendFrame(session, frame);
    }

    protected void cancelScheduledFuture(WebsocketSession session) {
        ScheduledFuture<?> future = scheduledFutureMap.get(session.sessionID);
        if (future != null) {
            future.cancel(false);
        }
    }
}
