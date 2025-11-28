package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.*;
import nl.radiantrealm.library.util.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class WebsocketHandshake {
    private static final Logger logger = Logger.getLogger(WebsocketHandshake.class);
    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private WebsocketHandshake() {}

    public static HttpResponse perform(HttpRequest request) {
        String secretKey = request.headers().getFirst("Sec-WebSocket-Key");

        if (secretKey == null) {
            throw new HttpException(HttpResponse.status(StatusCode.BAD_REQUEST, "Provide Sec-WebSocket-Key"));
        }

        String combined = secretKey + GUID;
        MessageDigest digest = getMessageDigest();
        byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
        String acceptKey = Base64.getEncoder().encodeToString(hash);

        HttpResponse response = HttpResponse.status(StatusCode.SWITCHING_PROTOCOLS);
        response.headers().add("Upgrade", "websocket");
        response.headers().add("Connection", "Upgrade");
        response.headers().add("Sec-WebSocket-Accept", acceptKey);
        return response;
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-1 algorithm not found", e);
            throw new HttpException(HttpResponse.status(StatusCode.SERVER_ERROR));
        }
    }
}
