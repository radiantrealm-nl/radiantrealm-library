package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.net.io.SocketConnection;
import nl.radiantrealm.library.net.io.SocketEngine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HttpClient extends SocketEngine {
    private final Queue<PendingConnection> pendingConnections = new ConcurrentLinkedQueue<>();
    private final Map<HttpConnection, CompletableFuture<HttpConnection>> completableConnectionMap = new ConcurrentHashMap<>();
    private final Map<HttpConnection, CompletableFuture<HttpResponse>> completableRequestMap = new ConcurrentHashMap<>();

    public HttpClient(HttpConfiguration configuration) throws IOException {
        super(configuration);
    }

    private record PendingConnection(
            CompletableFuture<HttpConnection> future,
            InetSocketAddress socketAddress
    ) {}

    public HttpConnection openConnection(InetSocketAddress socketAddress) {
        CompletableFuture<HttpConnection> future = new CompletableFuture<>();
        pendingConnections.add(new PendingConnection(
                future,
                socketAddress
        ));

        selector.wakeup();

        try {
            return future.get();
        } catch (Exception e) {
            logger.error("Exception while awaiting connection", e);
            return null;
        }
    }

    public HttpResponse sendRequest(HttpConnection connection, HttpRequest request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        completableRequestMap.put(connection, future);
        connection.sendRequest(request);

        try {
            return future.get();
        } catch (Exception e) {
            logger.error("Exception while awaiting connection", e);
            return null;
        }
    }

    public HttpResponse sendRequest(InetSocketAddress socketAddress, HttpRequest request) {
        try (HttpConnection connection = openConnection(socketAddress)) {
            return sendRequest(connection, request);
        }
    }

    @Override
    protected void processKeyActions() {
        while (!pendingConnections.isEmpty()) {
            PendingConnection pendingConnection = pendingConnections.poll();
            SocketChannel channel = null;

            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(pendingConnection.socketAddress);

                SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
                HttpConnection connection = new HttpConnection(this, channel);
                key.attach(connection);
                completableConnectionMap.put(connection, pendingConnection.future);
            } catch (IOException e) {
                logger.error("Exception while opening channel", e);
                pendingConnection.future.completeExceptionally(e);

                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ignored) {}
                }
            }
        }

        super.processKeyActions();
    }

    @Override
    protected void onRead(SelectionKey key, SocketConnection socketConnection) {
        if (key.attachment() instanceof HttpConnection connection && completableRequestMap.get(connection) instanceof CompletableFuture<HttpResponse> future) {
            executorService.submit(() -> {
                try {
                    HttpResponse response = connection.parser.parseResponse();

                    if (response == null) {
                        return;
                    }

                    completableRequestMap.remove(connection);
                    future.complete(response);

                    if (connection.awaitClosing.get()) {
                        connection.silentClose();
                    }
                } catch (RuntimeException e) {
                    logger.warning("Uncaught Runtime exception", e);
                    completableRequestMap.remove(connection);
                    future.completeExceptionally(e);
                    connection.silentClose();
                }
            });
        }
    }

    @Override
    protected void onDisconnect(SelectionKey key, SocketConnection socketConnection) {
        if (key.attachment() instanceof HttpConnection connection) {
            completableRequestMap.remove(connection);
        }
    }

    @Override
    protected void handleConnect(SelectionKey key) {
        if (key.attachment() instanceof HttpConnection connection && completableConnectionMap.get(connection) instanceof CompletableFuture<HttpConnection> future) {
            try {
                if (connection.channel.finishConnect()) {
                    key.interestOps(SelectionKey.OP_READ);
                    completableConnectionMap.remove(connection);
                    future.complete(connection);
                }
            } catch (IOException e) {
                logger.error("Exception while connecting channel", e);
                completableConnectionMap.remove(connection);
                future.completeExceptionally(e);
                connection.silentClose();
            }
        }
    }
}
