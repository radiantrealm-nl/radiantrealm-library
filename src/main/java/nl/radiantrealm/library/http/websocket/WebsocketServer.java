package nl.radiantrealm.library.http.websocket;

import nl.radiantrealm.library.utils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WebsocketServer implements AutoCloseable {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final ExecutorService executorService;
    protected final Map<String, WebsocketEndpoint> endpointMap = new HashMap<>();

    protected final Selector selector;
    protected final WebsocketConfiguration configuration;
    protected final ServerSocketChannel serverChannel;
    protected final AtomicBoolean isRunning = new AtomicBoolean(true);

    public WebsocketServer(WebsocketConfiguration configuration) throws IOException {
        this.executorService = Executors.newFixedThreadPool(configuration.workingIOThreads());
        this.selector = Selector.open();
        this.configuration = configuration;
        this.serverChannel = buildServerChannel();
        executorService.submit(this::IOLoop);
    }

    @Override
    public void close() throws IOException {
        isRunning.set(false);
        selector.wakeup();
        selector.close();
        serverChannel.close();
    }

    protected void registerEndpoint(String path, WebsocketEndpoint endpoint) {
        endpointMap.put(path, endpoint);
    }

    protected ServerSocketChannel buildServerChannel() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(configuration.socketAddress());
        channel.register(selector, SelectionKey.OP_ACCEPT);
        return channel;
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
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        acceptClientChannel();
                    }

                    if (key.isReadable() && key.attachment() instanceof WebsocketSession session) {
                        readInputBytes(session);
                    }
                }
            } catch (IOException e) {
                logger.error("Unexpected error during main IO Loop.", e);
            }
        }
    }

    protected void acceptClientChannel() throws IOException {
        SocketChannel channel = serverChannel.accept();

        if (channel != null) {
            channel.configureBlocking(false);
            executorService.submit(() -> {
                try {
                    String path = WebsocketHandshake.perform(channel);
                    WebsocketEndpoint endpoint = endpointMap.get(path);

                    if (endpoint == null) {
                        channel.close();
                    } else {
                        WebsocketSession session = buildWebsocketSession(channel, path);
                        channel.register(selector, SelectionKey.OP_READ, session);
                        endpoint.onOpen(session);
                    }
                } catch (Exception e) {
                    logger.error("Failed to accept client channel.", e);
                }
            });
        }
    }

    protected WebsocketSession buildWebsocketSession(SocketChannel channel, String path) {
        return new WebsocketSession(
                configuration,
                selector,
                path,
                UUID.randomUUID().toString(),
                channel
        );
    }

    protected void readInputBytes(WebsocketSession session) throws IOException {
        SocketChannel channel = session.channel;
        ByteBuffer buffer = ByteBuffer.allocate(configuration.incomingBufferSize());

        int bytesRead = channel.read(buffer);
        if (bytesRead < 0) {
            session.close();
            return;
        } else if (bytesRead > 0) {
            byte[] data = new byte[bytesRead];
            buffer.flip().get(data);
            session.appendInputBytes(data);
        }

        executorService.submit(() -> {
            try {
                boolean sufficientBytes;

                do {
                    sufficientBytes = awaitInputBytes(session);
                } while (sufficientBytes);
            } catch (IOException e) {
                logger.error("An error occured while waiting for input bytes.", e);
            }
        });
    }

    protected boolean awaitInputBytes(WebsocketSession session) throws IOException {
        try {
            byte[] bytes = session.getInputBytes();

            if (bytes.length < 2) {
                return false;
            }

            boolean finalMessage = (bytes[0] & 0x80) != 0;
            if (!configuration.allowFragmentation() && !finalMessage) {
                throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR);
            }

            int initialPayloadLength = bytes[1] & 0x7F;
            int headerOffset = 2;
            long payloadLength;

            if (initialPayloadLength == 126) {
                if (bytes.length < 4) return false;
                ByteBuffer buffer = ByteBuffer.wrap(bytes, 2, 2);
                payloadLength = Short.toUnsignedInt(buffer.getShort());
                headerOffset += 2;
            } else if (initialPayloadLength == 127) {
                if (bytes.length < 10) return false;
                ByteBuffer buffer = ByteBuffer.wrap(bytes, 2, 8);
                payloadLength = buffer.getLong();
                headerOffset += 8;
            } else {
                payloadLength = initialPayloadLength;
            }

            if (payloadLength > Integer.MAX_VALUE) {
                throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG);
            }

            boolean isMasked = (bytes[1] & 0x80) != 0;
            long frameLength = headerOffset + (isMasked ? 4 : 0) + payloadLength;
            if (frameLength > Integer.MAX_VALUE) {
                throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG);
            }

            if (bytes.length < frameLength) {
                return false;
            }

            WebsocketOperatorCode operatorCode = WebsocketOperatorCode.getWsopCode(bytes[0] & 0x0F);
            if (operatorCode == null) {
                throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR);
            }

            byte[] payload = new byte[(int) payloadLength];
            System.arraycopy(bytes, headerOffset + (isMasked ? 4 : 0), payload, 0, (int) payloadLength);
            session.consumeInputBytes((int) frameLength);

            if (isMasked) {
                byte[] maskKey = new byte[4];
                System.arraycopy(bytes, headerOffset, maskKey, 0, 4);

                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= maskKey[i % 4];
                }
            }

            WebsocketEndpoint endpoint = endpointMap.get(session.path);

            if (endpoint != null) {
                endpoint.onFrame(session, new WebsocketFrame(
                        operatorCode,
                        finalMessage,
                        payload
                ));
            }
            return true;
        } catch (WebsocketException e) {
            session.sendFrame(e.exitCode.generateFrame());
            session.close();
            return false;
        } catch (IOException e) {
            session.sendFrame(WebsocketExitCode.PROTOCOL_ERROR.generateFrame());
            session.close();
            return false;
        }
    }
}
