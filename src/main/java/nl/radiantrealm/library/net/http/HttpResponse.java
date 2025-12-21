package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.util.json.JsonObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record HttpResponse(
        HttpVersion version,
        int statusCode,
        String reasonPhrase,
        HttpHeaders headers,
        byte[] body
) {
    public HttpResponse(
            HttpVersion version,
            int statusCode,
            String reasonPhrase,
            HttpHeaders headers,
            byte[] body
    ) {
        this.version = Objects.requireNonNull(version);
        this.statusCode = statusCode;
        this.reasonPhrase = Objects.requireNonNull(reasonPhrase);
        this.headers = Objects.requireNonNull(headers);
        this.body = Objects.requireNonNull(body);
    }

    public static HttpResponse wrap(StatusCode statusCode) {
        return new HttpResponse(
                HttpVersion.HTTP_1_1,
                statusCode.code,
                statusCode.message,
                new HttpHeaders(),
                new byte[0]
        );
    }

    public static HttpResponse wrap(StatusCode statusCode, MediaType mediaType, byte[] body) {
        HttpHeaders headers = new HttpHeaders();

        if (Objects.requireNonNull(body).length > 0) {
            headers.add("Content-Length", String.valueOf(body.length));
            headers.add("Content-Type", Objects.requireNonNull(mediaType).type);
        }

        return new HttpResponse(
                HttpVersion.HTTP_1_1,
                statusCode.code,
                statusCode.message,
                headers,
                body
        );
    }

    public static HttpResponse wrap(StatusCode statusCode, String message) {
        JsonObject object = new JsonObject();
        object.put(switch (Objects.requireNonNull(statusCode).code) {
            case 200, 201, 202, 206 -> "info";
            case 301, 302, 307, 308 -> "redirect";
            default -> "error";
        }, message == null ? statusCode.message : message);

        return wrap(statusCode, MediaType.JSON, object.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeBody) {
        String headerString = headers.toString();
        String responseLine = String.format(
                "%s %s %s\r\n",
                version.version,
                statusCode,
                reasonPhrase
        );

        int length = 2 + responseLine.length() + headerString.length() + (includeBody ? body.length : 0);
        StringBuilder builder = new StringBuilder(length);
        builder.append(responseLine);
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
