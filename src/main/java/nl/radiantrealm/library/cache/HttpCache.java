package nl.radiantrealm.library.cache;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.http.model.HttpException;
import nl.radiantrealm.library.http.model.HttpTools;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class HttpCache<K, V> extends AbstractCache<K, V> implements HttpHandler {
    private final int statusCode;
    private final String mediaType;

    public HttpCache(@NotNull CachingStrategy strategy, int statusCode, String mediaType) {
        super(strategy);

        this.statusCode = statusCode;
        this.mediaType = mediaType;
    }

    public HttpCache(@NotNull CachingStrategy strategy, StatusCode statusCode, MediaType mediaType) {
        this(
                strategy,
                statusCode.code,
                mediaType.type
        );
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpTools tools = new HttpTools(exchange);

        try {
            K key = getKey(exchange);
            tools.sendResponse(statusCode, mediaType, valueToString(get(key)));
        } catch (HttpException e) {
            tools.sendResponse(e.response);
        } catch (Exception e) {
            tools.sendStatusResponse(StatusCode.SERVER_ERROR);
        }
    }

    protected abstract String valueToString(V value);
    protected abstract K getKey(HttpExchange exchange) throws Exception;
}
