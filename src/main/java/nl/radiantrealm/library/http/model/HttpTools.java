package nl.radiantrealm.library.http.model;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HttpTools(HttpExchange exchange, InputStream inputStream, OutputStream outputStream) {

    public HttpTools(HttpExchange exchange) {
        this(
                exchange,
                exchange.getRequestBody(),
                exchange.getResponseBody()
        );
    }

    public void sendResponse(int statusCode, String mediaType, String body) throws IOException {
        byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", mediaType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        outputStream.write(bytes);
    }

    public void sendResponse(StatusCode statusCode, MediaType mediaType, String body) throws IOException {
        sendResponse(statusCode.code, mediaType.type, body);
    }

    public void sendResponse(StatusCode statusCode, JsonObject object) throws IOException {
        sendResponse(statusCode, MediaType.JSON, object.toString());
    }

    public void sendResponse(HttpResponse response) throws IOException {
        sendResponse(response.statusCode(), response.mediaType(), response.responseBody());
    }

    public void sendStatusResponse(StatusCode statusCode, String key, String message) throws IOException {
        JsonObject object = new JsonObject();
        object.add(key, message);
        sendResponse(statusCode, object);
    }

    public void sendStatusResponse(StatusCode statusCode, String message) throws IOException {
        String key = statusCode.keyType();

        if (key == null) {
            sendResponse(statusCode, MediaType.JSON, null);
        } else {
            sendStatusResponse(statusCode, key, message);
        }
    }

    public void sendStatusResponse(StatusCode statusCode) throws IOException {
        sendStatusResponse(statusCode, statusCode.message);
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
