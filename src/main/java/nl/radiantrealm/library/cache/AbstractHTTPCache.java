package nl.radiantrealm.library.cache;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.http.model.HttpException;
import nl.radiantrealm.library.http.model.HttpTools;

import java.io.IOException;

public abstract class AbstractHTTPCache<K, V> extends AbstractCache<K, V> implements HttpHandler {
    private final String mediaType;

    public AbstractHTTPCache(CachingStrategy strategy, String mediaType) {
        super(strategy);

        this.mediaType = mediaType;
    }

    public AbstractHTTPCache(CachingStrategy strategy, MediaType mediaType) {
        this(
                strategy,
                mediaType.type
        );
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpTools tools = new HttpTools(exchange);

        try {
            K key = getKey(exchange);

            if (contains(key)) {
                sendResponse(tools, get(key));
            } else {
                V value = get(key);
                put(key, value);
                sendResponse(tools, value);
            }
        } catch (HttpException e) {
            tools.sendResponse(e.response);
        } catch (Exception e) {
            tools.sendStatusResponse(StatusCode.SERVER_ERROR);
        }
    }

    protected abstract String valueToString(V value);
    protected abstract K getKey(HttpExchange exchange) throws Exception;

    protected void sendResponse(HttpTools tools, V value) throws IOException {
        tools.sendResponse(200, mediaType, valueToString(value));
    }
}
