package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.net.http.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public abstract class HttpCache<K, V> extends AbstractCache<K, V> implements HttpHandler {

    public HttpCache(@NotNull CachingStrategy strategy) {
        super(strategy);
    }

    protected abstract K getKey(HttpExchange exchange);

    @Override
    public void handle(HttpExchange exchange) {
        try (exchange) {
            K key = getKey(exchange);
            exchange.sendResponse(buildHttpResponse(get(key)));
        } catch (HttpException e) {
            exchange.sendResponse(e.response);
        } catch (Exception e) {
            exchange.sendResponse(HttpResponse.status(StatusCode.SERVER_ERROR));
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
