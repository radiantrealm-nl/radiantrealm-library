package nl.radiantrealm.library.http.context;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.*;

public record HttpRequestContext(
        String method,
        URI requestURI,
        HttpClient.Version version,
        Headers headers,
        byte[] body
) {
    public HttpRequestContext(HttpExchange exchange) throws IOException {
        this(
                exchange.getRequestMethod(),
                exchange.getRequestURI(),
                getHttpVersion(exchange.getProtocol()),
                exchange.getRequestHeaders(),
                exchange.getRequestBody().readAllBytes()
        );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s %s %s\r\n", method, requestURI, printHttpVersion(version)));

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

    public static HttpRequestContext parse(String input) {
        String[] parts = input.split("\r\n\r\n", 2);
        String[] lines = parts[0].split("\r\n");
        String[] requestLine = lines[0].split(" ");

        if (requestLine.length < 3) {
            throw new IllegalArgumentException("Invalid request line");
        }

        String method = requestLine[0];
        URI requestURI = URI.create(requestLine[1]);
        HttpClient.Version version = getHttpVersion(requestLine[2]);
        Headers headers = parseHeaders(lines);
        byte[] body = parts.length > 1 ? parts[1].getBytes(StandardCharsets.UTF_8) : new byte[0];

        return new HttpRequestContext(
                method,
                requestURI,
                version,
                headers,
                body
        );
    }

    public static HttpClient.Version getHttpVersion(String string) {
        return switch (string) {
            case "HTTP/1.1" -> HttpClient.Version.HTTP_1_1;
            case "HTTP/2" -> HttpClient.Version.HTTP_2;
            default -> throw new IllegalArgumentException("Unsupported HTTP version: " + string);
        };
    }

    public static String printHttpVersion(HttpClient.Version version) {
        return switch (version) {
            case HTTP_1_1 -> "HTTP/1.1";
            case HTTP_2 -> "HTTP/2";
        };
    }

    public static Headers parseHeaders(String[] lines) {
        Map<String, List<String>> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            if (line.isEmpty()) {
                break;
            }

            int separator = lines[i].indexOf(':');
            if (separator > 0) {
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }

        return new Headers(headers);
    }

    public Map<String, HttpCookie> getCookies() {
        Map<String, HttpCookie> map = new HashMap<>();
        List<String> list = headers.get("Cookie");

        for (String string : list) {
            for (HttpCookie cookie : HttpCookie.parse(string)) {
                map.put(cookie.getName(), cookie);
            }
        }

        return map;
    }

    public HttpCookie getCookie(String name) {
        List<String> list = headers.get("Cookie");

        for (String string : list) {
            for (HttpCookie cookie : HttpCookie.parse(string)) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }

        return null;
    }
}
