package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.util.json.JsonObject;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record HttpResponse(
        HttpVersion version,
        int statusCode,
        String reasonPhrase,
        HttpHeaders headers,
        byte[] body
) {
    public static HttpResponse wrap(StatusCode statusCode, MediaType mediaType, byte[] body) {
        HttpHeaders headers = new HttpHeaders();

        if (body.length > 0) {
            headers.add("Content-Length", String.valueOf(body.length));
            headers.add("Content-Type", mediaType.type);
        }

        return new HttpResponse(
                HttpVersion.HTTP_1_1,
                statusCode.code,
                statusCode.message,
                headers,
                body
        );
    }

    public static HttpResponse status(StatusCode statusCode) {
        return new HttpResponse(
                HttpVersion.HTTP_1_1,
                statusCode.code,
                statusCode.message,
                new HttpHeaders(),
                new byte[0]
        );
    }

    public static HttpResponse status(StatusCode statusCode, String message) {
        JsonObject object = new JsonObject();
        object.add(switch (statusCode.code) {
            case 200, 201, 202, 206 -> "info";
            case 301, 302, 307, 308 -> "redirect";
            default -> "error";
        }, message);

        return wrap(statusCode, MediaType.JSON, object.toString().getBytes());
    }

    public byte[] getBytes(Charset charset) {
        String responseLine = String.format(
                "%s %s %s\r\n",
                version.version,
                statusCode,
                reasonPhrase
        );

        StringBuilder builder = new StringBuilder(responseLine);
        builder.append(headers.toString());
        builder.append("\r\n");

        byte[] headerBytes = builder.toString().getBytes(charset);
        byte[] resultBytes = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, resultBytes, 0, headerBytes.length);
        System.arraycopy(body, 0, resultBytes, headerBytes.length, body.length);
        return resultBytes;
    }

    public byte[] getBytes() {
        return getBytes(StandardCharsets.UTF_8);
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(getBytes());
    }
}
