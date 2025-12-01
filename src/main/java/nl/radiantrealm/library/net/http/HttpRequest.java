package nl.radiantrealm.library.net.http;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record HttpRequest(
        String requestMethod,
        URI requestURI,
        HttpVersion version,
        HttpHeaders headers,
        byte[] body
) {
    public byte[] getBytes(Charset charset) {
        String requestLine = String.format(
                "%s %s %s\r\n",
                requestMethod,
                requestURI.toString(),
                version.version
        );

        StringBuilder builder = new StringBuilder(requestLine);
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
