package nl.radiantrealm.library.http.context;

import com.sun.net.httpserver.Headers;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public record HttpResponseContext(
        HttpClient.Version version,
        int statusCode,
        String reasonPhrase,
        Headers headers,
        byte[] body
) {

    public HttpResponseContext(StatusCode statusCode, MediaType mediaType, byte[] body) {
        this(
                HttpClient.Version.HTTP_1_1,
                statusCode.code,
                statusCode.message,
                Headers.of(Map.of(
                        "Content-Type", List.of(mediaType.type)
                )),
                body
        );
    }

    public HttpResponseContext(StatusCode statusCode, JsonObject object) {
        this(
                statusCode,
                MediaType.JSON,
                object.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    public HttpResponseContext(StatusCode statusCode) {
        this(
                statusCode,
                statusCode.toJson()
        );
    }

    public HttpResponseContext(StatusCode statusCode, String message) {
        this(
                statusCode,
                statusCode.toJson(message)
        );
    }

    public HttpResponseContext(StatusCode statusCode, String key, String message) {
        this(
                statusCode,
                statusCode.toJson(key, message)
        );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s %s %s\r\n", version.name(), statusCode, reasonPhrase));

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String string : entry.getValue()) {
                builder.append(String.format("%s: %s\r\n", entry.getKey(), string));
            }
        }

        builder.append(headers.isEmpty() ? "\r\n\r\n" : "\r\n");

        for (byte b : body) {
            builder.append(b);
        }

        return builder.toString();
    }

    public static HttpResponseContext parse(String input) {
        String[] parts = input.split("\r\n\r\n", 2);
        String[] lines = parts[0].split("\r\n");
        String[] statusLine = lines[0].split(" ");

        if (statusLine.length < 3) {
            throw new IllegalArgumentException("Invalid status line");
        }

        HttpClient.Version version = HttpRequestContext.getHttpVersion(statusLine[0]);
        int statusCode = Integer.parseInt(statusLine[1]);
        String reasonPhrase = statusLine[2];
        Headers headers = HttpRequestContext.parseHeaders(lines);
        byte[] body = parts.length > 1 ? parts[1].getBytes(StandardCharsets.UTF_8) : new byte[0];

        return new HttpResponseContext(
                version,
                statusCode,
                reasonPhrase,
                headers,
                body
        );
    }
}
