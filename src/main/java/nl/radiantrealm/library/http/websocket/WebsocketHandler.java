package nl.radiantrealm.library.http.websocket;

import nl.radiantrealm.library.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class WebsocketHandler implements WebsocketEndpoint {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final Map<WebsocketSession, Long> lastActivityMillis = new HashMap<>();
    protected final Set<String> acknowledgedClosingHandshake = new HashSet<>();

    protected final Map<String, ScheduledFuture<?>> scheduledFutureMap = new HashMap<>();
    protected final ScheduledExecutorService executorService;
    protected final WebsocketConfiguration configuration;

    public WebsocketHandler(WebsocketConfiguration configuration) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.configuration = configuration;

        executorService.scheduleAtFixedRate(
                this::checkSessionTimeout,
                configuration.sessionTimeoutMillis(),
                configuration.sessionTimeoutMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public int availableCapacity() {
        return configuration.maxActiveSessions() - lastActivityMillis.size();
    }

    @Override
    public void onOpen(WebsocketSession session) {
        lastActivityMillis.put(session, System.currentTimeMillis());
    }

    @Override
    public void onFrame(WebsocketSession session, WebsocketFrame frame) {
        lastActivityMillis.put(session, System.currentTimeMillis());

        if (frame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
            if (!configuration.allowFragmentation()) {
                sendClosingHandshake(session, WebsocketExitCode.POLICY_VIOLATED.generateFrame());
                return;
            }

            session.addFragmentedFrame(frame);

            if (frame.finalMessage()) {
                List<WebsocketFrame> list = session.consumeFragmentedFrames();
                long payloadLength = 0;

                for (WebsocketFrame websocketFrame : list) {
                    payloadLength += websocketFrame.payload().length;
                }

                if (payloadLength > Integer.MAX_VALUE || payloadLength > configuration.maxPayloadLength()) {
                    sendClosingHandshake(session, WebsocketExitCode.MESSAGE_TOO_BIG.generateFrame());
                    return;
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) payloadLength);

                try {
                    for (WebsocketFrame websocketFrame : list) {
                        outputStream.write(websocketFrame.payload());
                    }
                } catch (IOException e) {
                    logger.error("Failed to write to output stream", e);
                    return;
                }

                handleFrame(session, new WebsocketFrame(
                        list.getFirst().operatorCode(),
                        true,
                        outputStream.toByteArray()
                ));
            }
        } else if (frame.finalMessage()) {
            handleFrame(session, frame);
        } else {
            session.addFragmentedFrame(frame);
        }
    }

    protected void handleFrame(WebsocketSession session, WebsocketFrame frame) {
        switch (frame.operatorCode()) {
            case UTF_8 -> onMessage(session, new String(frame.payload(), StandardCharsets.UTF_8));
            case BINARY -> onBinary(session, frame.payload());
            case CLOSE -> handleClose(session, frame);
            case PING -> handlePing(session, frame);
            case PONG -> handlePong(session);
        }
    }

    protected void sendFrame(WebsocketSession session, WebsocketFrame frame) {
        try {
            session.sendFrame(frame);
        } catch (IOException e) {
            logger.error("Failed to send frame", e);
        }
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

    protected void onMessage(WebsocketSession session, String message) {}
    protected void onBinary(WebsocketSession session, byte[] bytes) {}

    protected void onClose(WebsocketSession session) {}
    protected void onPing(WebsocketSession session) {}
    protected void onPong(WebsocketSession session) {}

    protected void cancelScheduledFuture(WebsocketSession session) {
        ScheduledFuture<?> future = scheduledFutureMap.remove(session.sessionID);
        if (future != null) {
            future.cancel(false);
        }
    }

    protected void sendClosingHandshake(WebsocketSession session, WebsocketFrame frame) {
        scheduledFutureMap.put(session.sessionID, executorService.schedule(() -> {
            closeSocket(session);
        }, configuration.closeTimeoutMillis(), TimeUnit.MILLISECONDS));

        acknowledgedClosingHandshake.add(session.sessionID);
        sendFrame(session, frame);
    }

    protected void handleClose(WebsocketSession session, WebsocketFrame frame) {
        byte[] bytes = frame.payload();

        if (bytes.length < 2 || bytes.length > 125) {
            sendClosingHandshake(session, WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid close frame"));
            return;
        }

        WebsocketExitCode exitCode = WebsocketExitCode.valueOfCode((bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF);
        if (exitCode == null) {
            sendClosingHandshake(session, WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid status code"));
            return;
        }

        if (!acknowledgedClosingHandshake.contains(session.sessionID)) {
            sendFrame(session, new WebsocketFrame(
                    WebsocketOperatorCode.CLOSE,
                    true,
                    frame.payload()
            ));
        }

        closeSocket(session);
    }

    protected void closeSocket(WebsocketSession session) {
        cancelScheduledFuture(session);
        lastActivityMillis.remove(session);
        acknowledgedClosingHandshake.remove(session.sessionID);

        try {
            session.close();
        } catch (IOException e) {
            logger.error("Failed to close session", e);
        }

        onClose(session);
    }

    protected void checkSessionTimeout() {
        long timestamp = System.currentTimeMillis();

        for (Map.Entry<WebsocketSession, Long> entry : lastActivityMillis.entrySet()) {
            WebsocketSession session = entry.getKey();

            if (scheduledFutureMap.containsKey(session.sessionID)) {
                continue;
            }

            if (timestamp - entry.getValue() > configuration.sessionTimeoutMillis()) {
                scheduledFutureMap.put(session.sessionID, executorService.schedule(() -> {
                    sendClosingHandshake(session, WebsocketExitCode.GOING_AWAY.generateFrame("PING timeout"));
                }, configuration.pongTimeoutMillis(), TimeUnit.MILLISECONDS));

                sendFrame(session, new WebsocketFrame(
                        WebsocketOperatorCode.PING,
                        true,
                        new byte[0]
                ));
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
