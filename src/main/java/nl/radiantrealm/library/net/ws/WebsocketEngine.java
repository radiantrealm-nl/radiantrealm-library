package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpConnection;
import nl.radiantrealm.library.net.io.SocketConnection;
import nl.radiantrealm.library.net.io.SocketEngine;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public abstract class WebsocketEngine extends SocketEngine {
    protected final Queue<HttpConnection> pendingUpgrades = new ConcurrentLinkedQueue<>();
    protected final Map<String, WebsocketSession> sessionMap = new ConcurrentHashMap<>();
    protected final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();
    protected final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    protected final WebsocketConfiguration configuration;

    public WebsocketEngine(WebsocketConfiguration configuration) throws IOException {
        super(configuration);
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
    protected void processKeyActions() {
        while (!pendingUpgrades.isEmpty()) {
            HttpConnection connection = pendingUpgrades.poll();

            try {
                WebsocketSession session = upgradeConnection(connection);
                sessionMap.put(session.sessionID, session);

                executorService.submit(() -> {
                    try {
                        onOpen(session);
                    } catch (WebsocketException e) {
                        sendClosingHandshake(session, e.frame);
                    } catch (RuntimeException e) {
                        logger.warning("Uncaught Runtime exception");
                    }
                });
            } catch (IOException e) {
                logger.error("Exceptionw hile upgrading channel", e);
                connection.silentClose();
            }
        }

        super.processKeyActions();
    }

    protected WebsocketSession upgradeConnection(HttpConnection connection) throws IOException {
        SelectionKey key = connection.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        WebsocketSession session = WebsocketSession.upgrade(this, connection);
        key.attach(session);
        selector.wakeup();
        return session;
    }

    @Override
    protected void onRead(SelectionKey key, SocketConnection socketConnection) {
        if (key.attachment() instanceof WebsocketSession session) {
            executorService.submit(() -> {
                try {
                    WebsocketFrame frame = session.parser.parse();

                    if (frame == null) {
                        return;
                    }

                    handleFrame(session, frame);
                } catch (WebsocketException e) {
                    sendClosingHandshake(session, e.frame);
                } catch (RuntimeException e) {
                    logger.warning("Uncaught Runtime exception", e);
                }
            });
        }
    }

    @Override
    protected void onWrite(SelectionKey key, SocketConnection socketConnection) {
        if (socketConnection.awaitClosing.get()) {
            onDisconnect(key, socketConnection);
            socketConnection.silentClose();
        }
    }

    @Override
    protected void onDisconnect(SelectionKey key, SocketConnection socketConnection) {
        if (socketConnection instanceof WebsocketSession session) {
            cancelScheduledFuture(session);

            if (sessionMap.remove(session.sessionID) != null) {
                executorService.submit(() -> {
                    try {
                        onClose(session);
                    } catch (RuntimeException e) {
                        logger.warning("Uncaught Runtime exception", e);
                    }
                });
            }
        }
    }

    protected void onOpen(WebsocketSession session) {}
    protected void onClose(WebsocketSession session) {}
    protected void onText(WebsocketSession session, String string) {}
    protected void onBinary(WebsocketSession session, byte[] bytes) {}

    protected void handleFrame(WebsocketSession session, WebsocketFrame frame) {
        session.lastActivityMillis.set(System.currentTimeMillis());

        switch (frame.operatorCode()) {
            case CONTINUE, TEXT, BINARY -> {
                WebsocketFrame websocketFrame = session.fragmentedFrame.glue(frame);

                if (websocketFrame == null) {
                    return;
                }

                switch (websocketFrame.operatorCode()) {
                    case TEXT -> onText(session, new String(websocketFrame.payload(), StandardCharsets.UTF_8));
                    case BINARY -> onBinary(session, websocketFrame.payload());
                }
            }

            case CLOSE -> handleClose(session, frame);
            case PING -> handlePing(session);
            case PONG -> handlePong(session);
        }
    }

    protected void checkSessionTimeout() {
        long timestamp = System.currentTimeMillis();

        for (WebsocketSession session : sessionMap.values()) {
            if (scheduledFutureMap.containsKey(session.sessionID)) {
                continue;
            }

            if (timestamp - session.lastActivityMillis.get() > configuration.sessionTimeoutMillis()) {
                scheduledFutureMap.put(session.sessionID, scheduledExecutorService.schedule(() -> {
                    sendClosingHandshake(session, WebsocketExitCode.GOING_AWAY.generateFrame("Ping timeout"));
                }, configuration.pingTimeoutMillis(), TimeUnit.MILLISECONDS));

                session.sendFrame(new WebsocketFrame(
                        WebsocketOperatorCode.PING,
                        true,
                        new byte[0]
                ));
            }
        }
    }

    protected void sendClosingHandshake(WebsocketSession session, WebsocketFrame frame) {
        scheduledFutureMap.put(session.sessionID, scheduledExecutorService.schedule(() -> {
            onDisconnect(session.key, session);
        }, configuration.closeTimeoutMillis(), TimeUnit.MILLISECONDS));

        session.acknowledgedClosing.set(true);
        session.sendFrame(frame);
    }

    protected void cancelScheduledFuture(WebsocketSession session) {
        ScheduledFuture<?> future = scheduledFutureMap.remove(session.sessionID);
        if (future != null) {
            future.cancel(false);
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

        if (session.acknowledgedClosing.get()) {
            onDisconnect(session.key, session);
            return;
        }

        session.sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.CLOSE,
                true,
                frame.payload()
        ));
    }

    protected void handlePing(WebsocketSession session) {
        cancelScheduledFuture(session);
        session.sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.PONG,
                true,
                new byte[0]
        ));
    }

    protected void handlePong(WebsocketSession session) {
        cancelScheduledFuture(session);
    }
}
