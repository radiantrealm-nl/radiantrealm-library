package nl.radiantrealm.library.http.websocket;

import com.sun.net.httpserver.Headers;
import nl.radiantrealm.library.http.HttpMethod;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.StatusCode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public interface WebsocketHandler {
    void onFrame(WebsocketFrame websocketFrame);

    default void handshake(HttpRequest request) throws Exception {
        if (!request.verifyRequestMethod(HttpMethod.GET)) {
            request.sendStatusResponse(StatusCode.INVALID_METHOD);
            return;
        }

        Headers clientHeaders = request.getRequestHeaders();

        if (!clientHeaders.getFirst("Upgrade").equalsIgnoreCase("websocket")) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST);
            return;
        } else if (!clientHeaders.getFirst("Upgrade").equalsIgnoreCase("connection")) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST);
            return;
        }

        String clientKey = clientHeaders.getFirst("Sec-WebSocket-Key");

        if (clientKey == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST);
            return;
        }

        String websocketKey = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-1").digest(
                        (clientKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)
                )
        );

        request.setResponseHeaders("Upgrade", "websocket");
        request.setResponseHeaders("Connection", "Upgrade");
        request.setResponseHeaders("Sec-Websocket-Accept", websocketKey);
        request.sendStatusResponse(StatusCode.SWITCHING_PROTOCOLS);
    }
}
