package nl.radiantrealm.library.http.model;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;

public record HttpRequestContext(
        String method,
        URI requestURI,
        HttpClient.Version version,
        Headers headers
) {
    public HttpRequestContext(HttpExchange exchange) {
        this(
                exchange.getRequestMethod(),
                exchange.getRequestURI(),
                getHttpVersion(exchange.getProtocol()),
                exchange.getRequestHeaders()
        );
    }

    public static HttpRequestContext parse(String input) {
        String[] lines = input.split("\r\n");
        String[] requestLineParts = lines[0].split(" ");

        if (requestLineParts.length < 3) {
            throw new IllegalArgumentException("Invalid request line");
        }

        String method = requestLineParts[0];
        URI requestURI = URI.create(requestLineParts[1]);
        HttpClient.Version version = getHttpVersion(requestLineParts[2]);

        Map<String, List<String>> headers = new TreeMap<>();
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

        return new HttpRequestContext(
                method,
                requestURI,
                version,
                new Headers(headers)
        );
    }

    private static HttpClient.Version getHttpVersion(String string) {
        return switch (string) {
            case "HTTP/1.1" -> HttpClient.Version.HTTP_1_1;
            case "HTTP/2" -> HttpClient.Version.HTTP_2;
            default -> throw new IllegalArgumentException("Unsupported HTTP version: " + string);
        };
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
