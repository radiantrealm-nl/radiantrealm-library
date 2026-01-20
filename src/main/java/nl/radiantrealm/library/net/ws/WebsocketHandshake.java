package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpException;
import nl.radiantrealm.library.net.http.HttpRequest;
import nl.radiantrealm.library.net.http.HttpResponse;
import nl.radiantrealm.library.net.http.StatusCode;
import nl.radiantrealm.library.util.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public class WebsocketHandshake {
    private static final Logger logger = Logger.getLogger(WebsocketHandshake.class);
    private static final SecureRandom random = new SecureRandom();

    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    private static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

    private WebsocketHandshake() {}

    public static HttpResponse performHandshake(HttpRequest request) {
        String secretKey = Objects.requireNonNull(request).headers().getFirst(SEC_WEBSOCKET_KEY);

        if (secretKey == null) {
            throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST));
        }

        if (!"upgrade".equalsIgnoreCase(request.headers().getFirst("Connection"))) {
            throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST));
        }

        if (!"websocket".equalsIgnoreCase(request.headers().getFirst("Upgrade"))) {
            throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST));
        }

        HttpResponse response = HttpResponse.wrap(StatusCode.SWITCHING_PROTOCOLS);
        response.headers().add("Connection", "Upgrade");
        response.headers().add("Upgrade", "websocket");
        response.headers().add(SEC_WEBSOCKET_ACCEPT, generateAcceptKey(secretKey));
        return response;
    }

    private static String generateAcceptKey(String secretKey) {
        try {
            String combined = secretKey + GUID;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-1 algorithm not found", e);
            throw new HttpException(HttpResponse.wrap(StatusCode.SERVER_ERROR));
        }
    }

    public static HttpRequest generateHandshake(String path) {
        HttpRequest request = HttpRequest.wrap("GET", Objects.requireNonNull(path));
        request.headers().add("Connection", "Upgrade");
        request.headers().add("Upgrade", "websocket");

        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        String secretKey = Base64.getEncoder().encodeToString(bytes);
        request.headers().add(SEC_WEBSOCKET_KEY, secretKey);
        return request;
    }

    public static boolean verifyHandshake(HttpResponse response, String secretKey) {
        String acceptKey = Objects.requireNonNull(response).headers().getFirst(SEC_WEBSOCKET_ACCEPT);

        if (acceptKey == null) {
            return false;
        }

        if (!"Upgrade".equals(response.headers().getFirst("Connection"))) {
            return false;
        }

        if (!"websocket".equals(response.headers().getFirst("Upgrade"))) {
            return false;
        }

        return acceptKey.equals(generateAcceptKey(Objects.requireNonNull(secretKey)));
    }
}
