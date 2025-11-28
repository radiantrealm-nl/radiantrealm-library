package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public abstract class WebsocketEndpoint extends WebsocketEngine {
    protected final Map<String, WebsocketSession> sessionMap = new ConcurrentHashMap<>();
    protected final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();
    protected final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public WebsocketEndpoint(WebsocketConfiguration configuration) throws IOException {
        super(configuration);
    }

    @Override
    public void start() {
        super.start();
        scheduledExecutorService.scheduleWithFixedDelay(
                this::checkSessionTimeout,
                5000,
                5000,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    protected void upgradeSession(WebsocketSession session) {
        sessionMap.put(session.sessionID, session);
        onOpen(session);
    }

    @Override
    protected void closeSession(WebsocketSession session) {
        sessionMap.remove(session.sessionID);
        cancelScheduledFuture(session);
        onClose(session);
    }

    @Override
    protected void onFrame(WebsocketSession session, WebsocketFrame frame) {
        session.lastActivityMillis.set(System.currentTimeMillis());
        Queue<WebsocketFrame> queue = session.fragmentedFrames;

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

                        if (websocketFrame.finalMessage()) {
                            break;
                        }
                    }

                    int index = 0;
                    byte[] resultBytes = new byte[(int) payloadLength];
                    for (WebsocketFrame websocketFrame : list) {
                        byte[] payloadBytes = websocketFrame.payload();
                        System.arraycopy(payloadBytes, 0, resultBytes, index, payloadBytes.length);
                        index += payloadBytes.length;
                    }

                    handleFrame(session, new WebsocketFrame(
                            list.getFirst().operatorCode(),
                            true,
                            resultBytes
                    ));
                }
            } else if (frame.finalMessage()) {
                handleFrame(session, frame);
            } else {
                queue.add(frame);
            }
        }

    }

    protected void handleFrame(WebsocketSession session, WebsocketFrame frame) {
        switch (frame.operatorCode()) {
            case UTF_8 -> onMessage(session, new String(frame.payload(), StandardCharsets.UTF_8));
            case BINARY -> onBinary(session, frame.payload());
            case CLOSE -> handleClose(session, frame);
            case PING -> handlePing(session, frame);
            case PONG -> handlePong(session);
        };
    }

    protected void onMessage(WebsocketSession session, String message) {}
    protected void onBinary(WebsocketSession session, byte[] bytes) {}

    protected void onOpen(WebsocketSession session) {}
    protected void onClose(WebsocketSession session) {}
    protected void onPing(WebsocketSession session) {}
    protected void onPong(WebsocketSession session) {}

    protected void sendFrame(WebsocketSession session, WebsocketFrame frame) {
        ByteBuffer buffer = ByteBuffer.wrap(frame.getBytes());
        VirtualByteBuffer outboundBuffer = session.outboundBuffer;

        synchronized (outboundBuffer) {
            outboundBuffer.add(buffer);
        }

        session.enableInterestOp(InterestOp.OP_WRITE);
        session.wakeup();
    }

    protected void sendMessage(WebsocketSession session, String message) {
        sendFrame(session, new WebsocketFrame(
                WebsocketOperatorCode.UTF_8,
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

    protected void sendClosingHandshake(WebsocketSession session, WebsocketFrame frame) {
        scheduledFutureMap.put(session.sessionID, scheduledExecutorService.schedule(() -> {
            try (session) {
                closeSession(session);
            }
        }, configuration.closeTimeoutMillis(), TimeUnit.MILLISECONDS));

        session.acknowledgedClosing.set(true);
        sendFrame(session, frame);
    }

    protected void checkSessionTimeout() {
        long timestamp = System.currentTimeMillis();

        for (WebsocketSession session : sessionMap.values()) {
            if (scheduledFutureMap.containsKey(session.sessionID)) {
                continue;
            }

            if (timestamp - session.lastActivityMillis.get() > configuration.closeTimeoutMillis()) {
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

    protected void cancelScheduledFuture(WebsocketSession session) {
        ScheduledFuture<?> future = scheduledFutureMap.get(session.sessionID);
        if (future != null) {
            future.cancel(false);
        }
    }

    protected void handleClose(WebsocketSession session, WebsocketFrame frame) {
        byte[] bytes = frame.payload();

        if (bytes.length < 2 || bytes.length > 125) {
            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid close frame"));
        }

        WebsocketExitCode exitCode = WebsocketExitCode.valueOfCode((bytes[0] & 0xFF) << 8 | bytes[1] & 0XFF);

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
                closeSession(session);
            }
        }
    }

    protected void handlePing(WebsocketSession session, WebsocketFrame frame) {
        cancelScheduledFuture(session);
        sendFrame(session, new WebsocketFrame(
                WebsocketOperatorCode.PONG,
                true,
                frame.payload()
        ));

        onPing(session);
    }

    protected void handlePong(WebsocketSession session) {
        cancelScheduledFuture(session);
        onPong(session);
    }
}
