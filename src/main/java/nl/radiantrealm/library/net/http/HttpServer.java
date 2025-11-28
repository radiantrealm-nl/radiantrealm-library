package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.net.io.Server;
import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServer extends Server {
    private final Map<String, HttpHandler> handlerMap = new HashMap<>();

    public HttpServer(InetSocketAddress socketAddress) throws IOException {
        super(socketAddress);
    }

    public void createContext(String path, HttpHandler handler) {
        handlerMap.put(path, handler);
    }

    @Override
    protected void handleRead(SelectionKey key) {
        if (key.attachment() instanceof HttpConnection connection) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);

            try {
                int read = connection.channel.read(buffer);

                if (read == -1) {
                    connection.close(true);
                    return;
                }

                if (read == 0) {
                    return;
                }
            } catch (IOException e) {
                logger.error("Exception while reading channel", e);
                sendClosingResponse(connection, HttpResponse.status(StatusCode.SERVER_ERROR));
                return;
            }

            connection.addInboundBuffer(buffer.flip());

            executorService.submit(() -> {
                try {
                    HttpRequest request = connection.aggregatedHttpRequest.parse();

                    if (request == null) {
                        return;
                    }

                    String path = request.requestURI().getPath();
                    for (Map.Entry<String, HttpHandler> entry : handlerMap.entrySet()) {
                        if (path.startsWith(entry.getKey())) {
                            entry.getValue().handle(new HttpExchange(connection, request));
                            return;
                        }
                    }

                    sendClosingResponse(connection, HttpResponse.status(StatusCode.NOT_FOUND));
                } catch (HttpException e) {
                    sendClosingResponse(connection, e.response);
                } catch (RuntimeException ignored) {
                    sendClosingResponse(connection, HttpResponse.status(StatusCode.SERVER_ERROR));
                }
            });
        }
    }

    @Override
    protected void handleWrite(SelectionKey key) {
        if (key.attachment() instanceof HttpConnection connection) {
            VirtualByteBuffer outboundBuffer = connection.outboundBuffer;

            try {
                synchronized (outboundBuffer) {
                    while (!outboundBuffer.isEmpty()) {
                        ByteBuffer buffer = ByteBuffer.wrap(outboundBuffer.peek(1024));
                        outboundBuffer.poll(connection.channel.write(buffer));

                        if (buffer.hasRemaining()) {
                            return;
                        }
                    }

                    if (outboundBuffer.isEmpty()) {
                        connection.disableInterestOp(InterestOp.OP_WRITE);
                        connection.wakeup();

                        if (connection.awaitClosing.get()) {
                            connection.close(true);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error while writing channel", e);
                sendClosingResponse(connection, HttpResponse.status(StatusCode.SERVER_ERROR));
            } catch (RuntimeException e) {
                sendClosingResponse(connection, HttpResponse.status(StatusCode.SERVER_ERROR));
            }
        }
    }

    @Override
    protected void handleAccept(SelectionKey key) {
        try {
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);

            SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new HttpConnection(this, clientKey, channel));
        } catch (IOException e) {
            logger.error("Exception when accepting channel", e);
        }
    }

    private void sendClosingResponse(HttpConnection connection, HttpResponse response) {
        try (connection) {
            VirtualByteBuffer outboundBuffer = connection.outboundBuffer;
            ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());

            synchronized (outboundBuffer) {
                outboundBuffer.add(buffer);
            }

            connection.enableInterestOp(InterestOp.OP_WRITE);
            connection.wakeup();
        }
    }
}
