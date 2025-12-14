package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpClient;
import nl.radiantrealm.library.net.http.HttpConnection;
import nl.radiantrealm.library.net.http.HttpRequest;
import nl.radiantrealm.library.net.http.HttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WebsocketClient extends WebsocketEngine {
    protected final Map<HttpConnection, CompletableFuture<WebsocketSession>> completableFutureMap = new ConcurrentHashMap<>();
    protected final HttpClient client;

    public WebsocketClient(WebsocketConfiguration configuration, HttpClient client) throws IOException {
        super(configuration);
        this.client = client;
    }

    public WebsocketSession connect(InetSocketAddress socketAddress, String path) {
        HttpRequest request = WebsocketHandshake.generateHandshake(path);
        String secretKey = request.headers().getFirst("Sec-WebSocket-Key");

        if (secretKey == null) {
            return null;
        }

        HttpConnection connection = client.openConnection(socketAddress);

        if (connection == null) {
            return null;
        }

        HttpResponse response = client.sendRequest(connection, request);

        if (response == null || !WebsocketHandshake.verifyHandshake(response, secretKey)) {
            return null;
        }

        CompletableFuture<WebsocketSession> future = new CompletableFuture<>();
        pendingUpgrades.add(connection);
        completableFutureMap.put(connection, future);
        connection.key.cancel();
        selector.wakeup();

        try {
            return future.get();
        } catch (Exception e) {
            logger.error("Exception while awaiting connection", e);
            return null;
        }
    }

    @Override
    protected WebsocketSession upgradeConnection(HttpConnection connection) throws IOException {
        CompletableFuture<WebsocketSession> future = completableFutureMap.remove(connection);

        if (future == null) {
            return super.upgradeConnection(connection);
        }

        try {
            WebsocketSession session = super.upgradeConnection(connection);
            future.complete(session);
            return session;
        } catch (IOException e) {
            future.completeExceptionally(e);
            throw e;
        }
    }
}
