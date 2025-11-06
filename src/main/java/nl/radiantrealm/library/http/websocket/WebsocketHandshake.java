package nl.radiantrealm.library.http.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsocketHandshake {
    private final static String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final static Pattern WEBSOCKET_KEY_PATTERN = Pattern.compile("Sec-WebSocket-Key: (.*)");

    private WebsocketHandshake() {}

    public static String perform(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);

        if (bytesRead <= 0) {
            return null;
        }

        buffer.flip();
        byte[] requestBytes = new byte[buffer.remaining()];
        buffer.get(requestBytes);
        String httpRequest = new String(requestBytes, StandardCharsets.UTF_8);

        String[] lines = httpRequest.split("\r\n");
        String requestPath = null;

        if (lines.length > 0) {
            String requestLine = lines[0];
            String[] parts = requestLine.split(" ");

            if (parts.length >= 2 && parts[0].equalsIgnoreCase("GET")) {
                requestPath = parts[1];
            }
        }

        if (requestPath == null) {
            return null;
        }

        Matcher matcher = WEBSOCKET_KEY_PATTERN.matcher(httpRequest);
        if (!matcher.find()) {
            return null;
        }

        String secWebSocketKey = matcher.group(1).trim();
        String secWebSocketAccept;

        try {
            secWebSocketAccept = generateAcceptKey(secWebSocketKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }

        String httpResponse = "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + secWebSocketAccept + "\r\n\r\n";

        ByteBuffer responseBuffer = ByteBuffer.wrap(httpResponse.getBytes(StandardCharsets.UTF_8));
        channel.write(responseBuffer);
        return requestPath;
    }

    private static String generateAcceptKey(String clientKey) throws NoSuchAlgorithmException {
        String combined = clientKey + MAGIC_STRING;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
