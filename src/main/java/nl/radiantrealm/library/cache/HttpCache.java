package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.net.http.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public abstract class HttpCache<K, V> extends AbstractCache<K, V> implements HttpHandler {

    public HttpCache(@NotNull CachingStrategy strategy) {
        super(strategy);
    }

    protected abstract K getKey(HttpConnection connection, HttpRequest request);

    @Override
    public void handle(HttpConnection connection, HttpRequest request) {
        try (connection) {
            K key = getKey(connection, request);
            connection.sendResponse(buildHttpResponse(get(key)));
        } catch (Exception e) {
            connection.sendResponse(HttpResponse.wrap(StatusCode.SERVER_ERROR));
        }
    }

    protected HttpResponse buildHttpResponse(V value) {
        return HttpResponse.wrap(
                StatusCode.OK,
                MediaType.JSON,
                value.toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
