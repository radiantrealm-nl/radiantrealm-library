package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.SocketConnection;
import nl.radiantrealm.library.net.io.SocketEngine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpServer extends SocketEngine {
    protected final Map<String, HttpHandler> handlerMap = new HashMap<>();
    protected final ServerSocketChannel serverChannel;

    public HttpServer(HttpConfiguration configuration, InetSocketAddress socketAddress) throws IOException {
        super(configuration);

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(socketAddress);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void createContext(String path, HttpHandler handler) {
        handlerMap.put(Objects.requireNonNull(path), Objects.requireNonNull(handler));
    }

    @Override
    protected void onRead(SelectionKey key, SocketConnection socketConnection) {
        if (key.attachment() instanceof HttpConnection connection) {
            executorService.submit(() -> {
                try {
                    HttpRequest request = connection.parser.parseRequest();

                    if (request == null) {
                        return;
                    }

                    for (Map.Entry<String, HttpHandler> entry : handlerMap.entrySet()) {
                        if (request.requestPath().startsWith(entry.getKey())) {
                            entry.getValue().handle(connection, request);
                            return;
                        }
                    }

                    try (connection) {
                        connection.sendResponse(HttpResponse.wrap(StatusCode.NOT_FOUND));
                    }
                } catch (HttpException e) {
                    try (connection) {
                        connection.sendResponse(e.response);
                    }
                } catch (RuntimeException e) {
                    logger.warning("Uncaught Runtime exception", e);

                    try (connection) {
                        connection.sendResponse(HttpResponse.wrap(StatusCode.BAD_REQUEST));
                    }
                } catch (Exception e) {
                    logger.error("Exception while handling request", e);

                    try (connection) {
                        connection.sendResponse(HttpResponse.wrap(StatusCode.SERVER_ERROR));
                    }
                }
            });
        }
    }

    @Override
    protected void onWrite(SelectionKey key, SocketConnection socketConnection) {
        if (socketConnection.awaitClosing.get()) {
            socketConnection.silentClose();
        }
    }

    @Override
    protected void handleAccept(SelectionKey key) {
        SocketChannel channel = null;

        try {
            channel = serverChannel.accept();

            if (channel != null) {
                channel.configureBlocking(false);

                SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
                clientKey.attach(new HttpConnection(this, channel));
            }
        } catch (IOException e) {
            logger.error("Exception while accepting channel", e);

            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
