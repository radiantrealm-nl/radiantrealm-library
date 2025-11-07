package nl.radiantrealm.library.http.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class WebsocketHandler implements WebsocketEndpoint {
    protected final Map<String, ScheduledFuture<?>> scheduledFutureMap = new HashMap<>();
    protected final Map<String, Long> lastActivityMillis = new HashMap<>();
    protected final Set<String> acknowledgedCloseFrame = new HashSet<>();
    protected final ScheduledExecutorService executorService;
    protected final WebsocketConfiguration configuration;

    public WebsocketHandler(WebsocketConfiguration configuration) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.configuration = configuration;
    }

    @Override
    public void onOpen(WebsocketSession session) throws IOException {
        lastActivityMillis.put(session.sessionID, System.currentTimeMillis());
    }

    @Override
    public void onFrame(WebsocketSession session, WebsocketFrame frame) throws IOException {
        if (frame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
            if (!configuration.allowFragmentation()) {
                sendClosingHandshake(session, WebsocketExitCode.PROTOCOL_ERROR);
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
                    sendClosingHandshake(session, WebsocketExitCode.MESSAGE_TOO_BIG);
                    return;
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) payloadLength);
                for (WebsocketFrame websocketFrame : list) {
                    outputStream.write(websocketFrame.payload());
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

    protected void handleFrame(WebsocketSession session, WebsocketFrame frame) throws IOException {
        lastActivityMillis.put(session.sessionID, System.currentTimeMillis());

        switch (frame.operatorCode()) {
            case UTF_8 -> onMessage(session, new String(frame.payload(), StandardCharsets.UTF_8));
            case BINARY -> onBinary(session, frame.payload());
            case CLOSE -> handleClose(session, frame);
            case PING -> handlePing(session, frame);
            case PONG -> handlePong(session, frame);
        }
    }

    protected void onMessage(WebsocketSession session, String message) throws IOException {}
    protected void onBinary(WebsocketSession session, byte[] bytes) throws IOException {}
    protected void onPing(WebsocketSession session) throws IOException {}
    protected void onPong(WebsocketSession session) throws IOException {}

    protected void handleClose(WebsocketSession session, WebsocketFrame frame) throws IOException {
        byte[] bytes = frame.payload();

        if (bytes.length < 2) {
            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR);
        } else if (bytes.length > 125) {
            throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG);
        }

        WebsocketExitCode exitCode = WebsocketExitCode.getExitCode((bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF);

        if (exitCode == null) {
            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR);
        }

        if (!acknowledgedCloseFrame.contains(session.sessionID)) {
            session.sendFrame(new WebsocketFrame(
                    WebsocketOperatorCode.CLOSE,
                    true,
                    frame.payload()
            ));
        }

        closeSocket(session);
    }

    protected void handlePing(WebsocketSession session, WebsocketFrame frame) throws IOException {
        session.sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.PONG,
                true,
                frame.payload()
        ));

        onPing(session);
    }

    protected void handlePong(WebsocketSession session, WebsocketFrame frame) throws IOException {
        ScheduledFuture<?> future = scheduledFutureMap.remove(session.sessionID);

        if (future != null) {
            future.cancel(false);
        }

        onPong(session);
    }

    protected void sendMessage(WebsocketSession session, String message) throws IOException {
        session.sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.UTF_8,
                true,
                message.getBytes(StandardCharsets.UTF_8)
        ));
    }

    protected void sendBinary(WebsocketSession session, byte[] bytes) throws IOException {
        session.sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.BINARY,
                true,
                bytes
        ));
    }

    protected void sendClosingHandshake(WebsocketSession session, WebsocketExitCode exitCode) throws IOException {
        sendClosingHandshake(session, exitCode, exitCode.message);
    }

    protected void sendClosingHandshake(WebsocketSession session, WebsocketExitCode exitCode, String reason) throws IOException {
        ScheduledFuture<?> future = executorService.schedule(() -> {
            try {
                closeSocket(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 5, TimeUnit.MILLISECONDS);

        scheduledFutureMap.put(session.sessionID, future);
        acknowledgedCloseFrame.add(session.sessionID);
        session.sendFrame(exitCode.generateFrame(reason));
    }

    protected void closeSocket(WebsocketSession session) throws IOException {
        ScheduledFuture<?> future = scheduledFutureMap.remove(session.sessionID);

        if (future != null) {
            future.cancel(false);
        }

        lastActivityMillis.remove(session.sessionID);
        acknowledgedCloseFrame.remove(session.sessionID);
        session.close();
        onClose(session);
    }
}
