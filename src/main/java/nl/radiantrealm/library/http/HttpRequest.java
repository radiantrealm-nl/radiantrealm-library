package nl.radiantrealm.library.http;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public record HttpRequest(HttpExchange exchange, InputStream inputStream, OutputStream outputStream) implements AutoCloseable {

    public HttpRequest(HttpExchange exchange) {
        this(exchange, exchange.getRequestBody(), exchange.getResponseBody());
    }

    @Override
    public void close() {
        exchange.close();
    }

    public String getRequestBody() {
        return new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public void sendResponse(int statusCode, String mimeType, String body) throws Exception {
        OutputStream stream = exchange.getResponseBody();

        if (body == null) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

            setResponseHeaders("Content-Type", mimeType);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            stream.write(bytes);
        }
    }

    public void sendResponse(StatusCode statusCode, MimeType mimeType, String body) throws Exception {
        sendResponse(statusCode.code, mimeType.type, body);
    }

    public void sendResponse(StatusCode statusCode, JsonObject object) throws Exception {
        sendResponse(statusCode, MimeType.JSON, object.toString());
    }

    public void sendStatusResponse(StatusCode statusCode, String key, String value) throws Exception {
        JsonObject object = new JsonObject();
        object.addProperty(key, value);
        sendResponse(statusCode, object);
    }

    public void sendStatusResponse(StatusCode statusCode) throws Exception {
        String key = statusCode.getKeyType();

        if (key == null) {
            key = "info";
        }

        sendStatusResponse(statusCode, key, statusCode.message);
    }

    public void setResponseHeaders(String key, String value) {
        exchange.getResponseHeaders().set(key, value);
    }

    public HttpMethod getRequestMethod() {
        return HttpMethod.getMethod(exchange.getRequestMethod());
    }

    public boolean verifyRequestMethod(HttpMethod method) {
        return getRequestMethod().equals(method);
    }

    public Headers getRequestHeaders() {
        return exchange.getRequestHeaders();
    }

    public Headers getResponseHeaders() {
        return exchange.getResponseHeaders();
    }
}
