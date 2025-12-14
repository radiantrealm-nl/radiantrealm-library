package nl.radiantrealm.library.net.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record HttpRequest(
        String requestMethod,
        String requestPath,
        HttpVersion version,
        HttpHeaders headers,
        byte[] body
) {
    public HttpRequest(
            String requestMethod,
            String requestPath,
            HttpVersion version,
            HttpHeaders headers,
            byte[] body
    ) {
        this.requestMethod = Objects.requireNonNull(requestMethod);
        this.requestPath = Objects.requireNonNull(requestPath);
        this.version = Objects.requireNonNull(version);
        this.headers = Objects.requireNonNull(headers);
        this.body = Objects.requireNonNull(body);
    }

    public static HttpRequest wrap(String requestMethod, String requestPath) {
        return new HttpRequest(
                requestMethod,
                requestPath,
                HttpVersion.HTTP_1_1,
                new HttpHeaders(),
                new byte[0]
        );
    }

    public static HttpRequest wrap(String requestMethod, String requestPath, MediaType mediaType, byte[] body) {
        HttpHeaders headers = new HttpHeaders();

        if (Objects.requireNonNull(body).length > 1) {
            headers.add("Content-Length", String.valueOf(body.length));
            headers.add("Content-Type", Objects.requireNonNull(mediaType).type);
        }

        return new HttpRequest(
                requestMethod,
                requestPath,
                HttpVersion.HTTP_1_1,
                headers,
                body
        );
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeBody) {
        String headerString = headers.toString();
        String requestLine = String.format(
                "%s %s %s\r\n",
                requestMethod,
                requestPath,
                version.version
        );

        int length = 2 + requestLine.length() + headerString.length() + (includeBody ? body.length : 0);
        StringBuilder builder = new StringBuilder(length);
        builder.append(requestLine);
        builder.append(headerString);
        builder.append("\r\n");

        if (includeBody) {
            for (byte b : body) {
                builder.append(String.format("%02X", b));
            }
        }

        return builder.toString();
    }

    public String toString(Charset charset) {
        String headerString = toString(false);

        int length = 2 + headerString.length() + body.length;
        StringBuilder builder = new StringBuilder(length);
        builder.append(headerString);

        if (body.length > 0) {
            builder.append(new String(body, charset));
        }

        return builder.toString();
    }

    public byte[] getBytes(Charset charset) {
        byte[] headerBytes = toString(false).getBytes(charset);
        byte[] resultBytes = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, resultBytes, 0, headerBytes.length);
        System.arraycopy(body, 0, resultBytes, headerBytes.length, body.length);
        return resultBytes;
    }

    public byte[] getBytes() {
        return getBytes(StandardCharsets.UTF_8);
    }
}
