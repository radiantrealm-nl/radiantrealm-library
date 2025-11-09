package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.http.server.ExtendedHttpHandler;
import nl.radiantrealm.library.http.context.HttpExchangeContext;
import nl.radiantrealm.library.http.context.HttpResponseContext;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.http.HttpException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class HttpCache<K, V> extends AbstractCache<K, V> implements ExtendedHttpHandler {

    public HttpCache(@NotNull CachingStrategy strategy) {
        super(strategy);
    }

    protected abstract K getKey(HttpExchangeContext exchangeContext) throws Exception;

    @Override
    public void handle(HttpExchangeContext exchangeContext) throws IOException {
        try (exchangeContext) {
            K key = getKey(exchangeContext);
            exchangeContext.sendResponse(buildHttpResponse(get(key)));
        } catch (HttpException e) {
            exchangeContext.sendResponse(e.responseContext);
        } catch (Exception e) {
            exchangeContext.sendStatusResponse(StatusCode.SERVER_ERROR);
        }
    }

    protected HttpResponseContext buildHttpResponse(V value) {
        return new HttpResponseContext(
                StatusCode.OK,
                MediaType.JSON,
                value.toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
