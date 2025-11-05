package nl.radiantrealm.library.http.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WebsocketServer implements AutoCloseable {
    protected final ExecutorService executorService = Executors.newFixedThreadPool(10);
    protected final Map<String, Set<WebsocketEndpoint>> listeners = new HashMap<>();

    protected final Selector selector;
    protected final WebsocketConfiguration configuration;
    protected final ServerSocketChannel serverChannel;
    protected final AtomicBoolean isRunning = new AtomicBoolean(true);

    public WebsocketServer(WebsocketConfiguration configuration) throws IOException {
        this.selector = Selector.open();
        this.configuration = configuration;
        this.serverChannel = buildServerChannel();
        executorService.submit(this::IOLoop);
    }

    @Override
    public void close() throws Exception {
        isRunning.set(false);
        selector.wakeup();
        selector.close();
        serverChannel.close();
    }

    protected void IOLoop() {
        while (isRunning.get()) {
            try {
                selector.wakeup();
                if (selector.select() == 0) {
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        acceptClientChannel();
                    }

                    if (key.isReadable() && key.attachment() instanceof WebsocketSession session) {
                        handleRead(session);
                    }

                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected ServerSocketChannel buildServerChannel() throws IOException {
        System.out.println("building server channel");
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(configuration.address());
        channel.register(selector, SelectionKey.OP_ACCEPT);
        return channel;
    }

    protected void acceptClientChannel() throws IOException {
        System.out.println("accepting client channel");
        SocketChannel channel = serverChannel.accept();

        if (channel != null) {
            channel.configureBlocking(false);
            executorService.submit(() -> {
                try {
                    if (WebsocketHandshake.perform(channel)) {
                        WebsocketSession session = buildWebsocketSession(channel);
                        channel.register(selector, SelectionKey.OP_READ, session);
                    } else {
                        channel.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    protected WebsocketSession buildWebsocketSession(SocketChannel channel) {
        return new WebsocketSession(
                configuration,
                UUID.randomUUID().toString(),
                channel
        );
    }

    protected void handleRead(WebsocketSession session) throws IOException {
        SocketChannel channel = session.channel;
        ByteBuffer buffer = ByteBuffer.allocate(configuration.incomingBufferSize());
        int bytesRead = channel.read(buffer);

        if (bytesRead < 0) {
            closeSocket(session, WebsocketExitCode.NO_DATA_RECEIVED);
            return;
        }

        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[bytesRead];
            buffer.get(data);
            session.appendInputBytes(data);
        }

        executorService.submit(() -> {
            boolean sufficientBytes;

            do {
                sufficientBytes = processFrames(session);
            } while (sufficientBytes);
        });
    }

    protected boolean processFrames(WebsocketSession session) {
        try {
            byte[] bytes = session.getInputBytes();

            if (bytes.length < 2) {
                return false;
            }

            boolean isMasked = (bytes[1] & 0x80) != 0;
            int initialPayloadLength = bytes[1] & 0x7F;

            int headerOffset = 2;
            long payloadLength;

            if (initialPayloadLength == 126) {
                if (bytes.length < 4) return false;
                payloadLength = ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
                headerOffset += 2;
            } else if (initialPayloadLength == 127) {
                if (bytes.length < 10) return false;
                ByteBuffer buffer = ByteBuffer.wrap(bytes, 2, 8);
                payloadLength = buffer.getLong();
                headerOffset += 8;
            } else {
                payloadLength = initialPayloadLength;
            }

            if (payloadLength > Integer.MAX_VALUE || payloadLength > configuration.maxPayloadLength()) {
                closeSocket(session, WebsocketExitCode.TOO_MUCH_DATA);
                return true;
            }

            int maskOffset = isMasked ? 4 : 0;
            int frameSize = headerOffset + maskOffset + (int) payloadLength;
            if (bytes.length < frameSize) {
                return false;
            }

            int headerSize = headerOffset  + maskOffset;
            byte[] payload = new byte[(int) payloadLength];
            System.arraycopy(bytes, headerSize, payload, 0, (int) payloadLength);

            if (isMasked) {
                byte[] maskKey = new byte[4];
                System.arraycopy(bytes, headerOffset, maskKey, 0, 4);
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= maskKey[i % 4];
                }
            }

            session.consumeInputBytes(frameSize);
            dispatchFrame(session, new WebsocketFrame(
                    WebsocketOperatorCode.getWsopCode(bytes[0] & 0x0F),
                    (bytes[0] & 0x80) != 0,
                    payload
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void dispatchFrame(WebsocketSession session, WebsocketFrame frame) throws IOException {
        if (frame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
            session.addFragmentedFrame(frame);

            if (frame.finalMessage()) {
                List<WebsocketFrame> list = session.consumeFragmentedFrames();
                long payloadLength = 0;

                for (WebsocketFrame websocketFrame : list) {
                    payloadLength += websocketFrame.payload().length;
                }

                if (payloadLength > Integer.MAX_VALUE || payloadLength > configuration.maxPayloadLength()) {
                    closeSocket(session, WebsocketExitCode.TOO_MUCH_DATA);
                    return;
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) payloadLength);
                for (WebsocketFrame websocketFrame : list) {
                    outputStream.write(websocketFrame.payload());
                }

//                for (WebsocketEndpoint endpoint : listeners.get(session.sessionID)) {
//                    endpoint.onFrame(session, new WebsocketFrame(
//                            list.getFirst().operatorCode(),
//                            true,
//                            outputStream.toByteArray()
//                    ));
//                }
            }
        } else if (frame.finalMessage()) {
//            for (WebsocketEndpoint endpoint : listeners.get(session.sessionID)) {
//                endpoint.onFrame(session, frame);
//            }
            System.out.println("Received: " + new String(frame.payload(), StandardCharsets.UTF_8));
            sendFrame(session, new WebsocketFrame(
                    WebsocketOperatorCode.UTF_8,
                    true,
                    frame.payload()
            ));
        } else {
            session.addFragmentedFrame(frame);
        }
    }

    protected void sendFrame(WebsocketSession session, WebsocketFrame frame) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(frame.toBytes());
        session.channel.write(buffer);
    }

    protected void closeSocket(WebsocketSession session, WebsocketExitCode exitCode) throws IOException {
        sendFrame(session, new WebsocketFrame(
                WebsocketOperatorCode.CLOSE,
                true,
                new byte[] {
                        (byte) (exitCode.code & 0xFF << 8),
                        (byte) (exitCode.code & 0xFF)
                }
        ));
        session.channel.close();
    }
}
