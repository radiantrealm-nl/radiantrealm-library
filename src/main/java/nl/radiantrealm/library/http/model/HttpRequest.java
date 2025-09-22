package nl.radiantrealm.library.http.model;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.http.enumerator.HttpMethod;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record HttpRequest(HttpExchange exchange, InputStream inputStream, OutputStream outputStream) implements AutoCloseable {

    public HttpRequest(HttpExchange exchange) {
        this(
                exchange,
                exchange.getRequestBody(),
                exchange.getResponseBody()
        );
    }

    @Override
    public void close() {
        exchange.close();
    }

    public void sendResponse(int statusCode, String mediaType, String body) throws Exception {
        byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", mediaType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        outputStream.write(bytes);
    }

    public void sendResponse(StatusCode statusCode, MediaType mediaType, String body) throws Exception {
        sendResponse(statusCode.code, mediaType.type, body);
    }

    public void sendResponse(StatusCode statusCode, JsonObject object) throws Exception {
        sendResponse(statusCode.code, MediaType.JSON.type, object == null ? "{}" : object.getAsString());
    }

    public void sendStatusResponse(StatusCode statusCode, String key, String message) throws Exception {
        JsonObject object = new JsonObject();
        object.addProperty(key, message);
        sendResponse(statusCode, object);
    }

    public void sendStatusResponse(StatusCode statusCode, String message) throws Exception {
        String key = statusCode.keyType();

        if (key == null) {
            sendResponse(statusCode, null);
        } else {
            sendStatusResponse(statusCode, key, message);
        }
    }

    public void sendStatusResponse(StatusCode statusCode) throws Exception {
        sendStatusResponse(statusCode, statusCode.message);
    }

    public Map<String, HttpCookie> getCookies() {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");

        if (cookies == null) {
            return Map.of();
        } else {
            return cookies.stream()
                    .flatMap(header -> HttpCookie.parse(header).stream())
                    .collect(Collectors.toMap(HttpCookie::getName, cookie -> cookie, (a, b) -> b));
        }
    }

    public HttpCookie getCookie(String key) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookies");

        if (cookies == null) {
            return null;
        } else {
            return cookies.stream()
                    .flatMap(header -> HttpCookie.parse(header).stream())
                    .filter(cookie -> cookie.getName().equals(key))
                    .findFirst()
                    .orElse(null);
        }
    }

    public String getRequestBody() {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public void aeb() {
        HttpMethod quiet = HttpMethod.valueOf("");
    }

    public Headers getRequestHeaders() {
        return exchange.getRequestHeaders();
    }

    public Headers getResponseHeaders() {
        return exchange.getResponseHeaders();
    }

    public String getRequestURI() {
        return exchange.getRequestURI().toString();
    }

    public void setResponseHeader(String key, String value) {
        exchange.getResponseHeaders().set(key, value);
    }
}
