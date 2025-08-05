package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.enumerator.MimeType;
import nl.radiantrealm.library.enumerator.StatusCode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public record HttpRequest(HttpExchange exchange, boolean keepAlive) {

    public String getRequestBody(InputStream stream) {
        try {
            return new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } finally {
            if (!keepAlive) {
                exchange.close();
            }
        }
    }

    public void sendResponse(int statusCode, String mimeType, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        try {
            setResponseHeader("Content-Type", mimeType);
            exchange.sendResponseHeaders(statusCode, bytes.length);

            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();
        } finally {
            if (!keepAlive) {
                exchange.close();
            }
        }
    }

    public void sendResponse(StatusCode statusCode, MimeType mimeType, String body) throws Exception {
        sendResponse(statusCode.getCode(), mimeType.getType(), body);
    }

    public void sendResponse(StatusCode statusCode, JsonObject object) throws Exception {
        sendResponse(statusCode, MimeType.JSON, object.toString());
    }

    public void setResponseHeader(String key, String value) {
        exchange.getResponseHeaders().set(key, value);
    }
}
