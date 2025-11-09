package nl.radiantrealm.library.http.context;

import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.http.enumerator.HttpMethod;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HttpExchangeContext(HttpExchange exchange) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        exchange.close();
    }

    public void sendResponse(int statusCode, String mediaType, String body) throws IOException {
        byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", mediaType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public void sendResponse(HttpResponseContext context) throws IOException {
        byte[] bytes = (context.body() == null ? new byte[0] : context.body());
        exchange.getResponseHeaders().putAll(context.headers());
        exchange.sendResponseHeaders(context.statusCode(), bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public void sendStatusResponse(StatusCode statusCode, String key, String message) throws IOException {
        JsonObject object = new JsonObject();
        object.add(key, message);
        sendResponse(statusCode.code, MediaType.JSON.type, object.toString());
    }

    public void sendStatusResponse(StatusCode statusCode, String message) throws IOException {
        String key = statusCode.keyType();

        if (key == null) {
            sendResponse(statusCode.code, MediaType.JSON.type, null);
        } else {
            sendStatusResponse(statusCode, key, message);
        }
    }

    public void sendStatusResponse(StatusCode statusCode) throws IOException {
        sendStatusResponse(statusCode, statusCode.message);
    }

    public HttpMethod getRequestMethod() {
        return HttpMethod.valueOf(exchange.getRequestMethod().toUpperCase());
    }

    public Map<String, HttpCookie> getCookies() {
        Map<String, HttpCookie> map = new HashMap<>();
        List<String> list = exchange.getRequestHeaders().get("Cookie");

        for (String string : list) {
            for (HttpCookie cookie : HttpCookie.parse(string)) {
                map.put(cookie.getName(), cookie);
            }
        }

        return map;
    }

    public HttpCookie getCooie(String key) {
        List<String> list = exchange.getRequestHeaders().get("Cookie");

        for (String string : list) {
            for (HttpCookie cookie : HttpCookie.parse(string)) {
                if (cookie.getName().equals(key)) {
                    return cookie;
                }
            }
        }

        return null;
    }
}
