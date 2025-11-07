package nl.radiantrealm.library.http.websocket;

import nl.radiantrealm.library.http.model.HttpRequestContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class WebsocketHandshake {
    private final static String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private WebsocketHandshake() {}

    public static void perform(SocketChannel channel, HttpRequestContext context) throws IOException {
        String secWebSocketKey = context.headers().getFirst("Sec-WebSocket-Key");

        if (secWebSocketKey == null) {
            return;
        }

        String secWebSocketAccept;
        try {
            secWebSocketAccept = generateAcceptKey(secWebSocketKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }

        String httpResponse = generateHttpResponsee(secWebSocketAccept);
        ByteBuffer responseBuffer = ByteBuffer.wrap(httpResponse.getBytes(StandardCharsets.UTF_8));
        channel.write(responseBuffer);
    }

    private static String generateAcceptKey(String clientKey) throws NoSuchAlgorithmException {
        String combined = clientKey + GUID;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private static String generateHttpResponsee(String acceptKey) {
        return "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
    }
}
