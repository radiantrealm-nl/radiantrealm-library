package nl.radiantrealm.library.net.http.cookie;

import nl.radiantrealm.library.net.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

public record Cookie(
        String name,
        String value
) {
    public static Cookie parse(String string) {
        String[] parts = string.split("=", 2);

        if (parts.length < 2) {
            return null;
        }

        return new Cookie(
                parts[0].trim(),
                parts[0].trim()
        );
    }

    public static List<Cookie> parse(HttpHeaders headers) {
        List<String> headerValues = headers.get("cookie");

        if (headerValues == null || headerValues.isEmpty()) {
            return List.of();
        }

        List<Cookie> cookies = new ArrayList<>();

        for (String headerValue : headerValues) {
            String[] split = headerValue.split(";");

            for (String string : split) {
                Cookie cookie = parse(string);

                if (cookie != null) {
                    cookies.add(cookie);
                }
            }
        }

        return cookies;
    }

    @Override
    public String toString() {
        return String.format(
                "%s=%s",
                name,
                value
        );
    }
}
