package nl.radiantrealm.library.http.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.http.HttpMethod;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.http.websocket.WebsocketHandler;
import nl.radiantrealm.library.utils.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public abstract class ApplicationRouter {
    private static final Logger logger = Logger.getLogger(ApplicationRouter.class);
    private final HttpServer server;

    public ApplicationRouter(int port) {
        this.server = createHttpServer(port);
    }

    private HttpServer createHttpServer(int port) {
        try {
            return HttpServer.create(new InetSocketAddress(port), 0);
        } catch (Exception e) {
            logger.error(String.format("Failed to setup HttpServer at port %d", port), e);
            return null;
        }
    }

    protected void register(String path, RequestHandler handler) {
        server.createContext(path, exchange -> {
            try {
                handler.handle(new HttpRequest(exchange));
            } catch (Exception e) {
                logger.error(String.format("Unexpected exception in %s.", handler.getClass().getSimpleName()), e);
            }
        });
    }

    protected void register(String path, WebsocketHandler websocketHandler) {
        register(path, handler -> {
            if (!handler.verifyRequestMethod(HttpMethod.GET)) {
                handler.sendStatusResponse(StatusCode.INVALID_METHOD);
                return;
            }

            Headers clientHeaders = handler.getRequestHeaders();

            if (!clientHeaders.getFirst("Upgrade").equalsIgnoreCase("websocket")) {
                handler.sendStatusResponse(StatusCode.BAD_REQUEST);
                return;
            } else if (!clientHeaders.getFirst("Upgrade").equalsIgnoreCase("connection")) {
                handler.sendStatusResponse(StatusCode.BAD_REQUEST);
                return;
            }

            String secretKey = clientHeaders.getFirst("Sec-WebSocket-Key");

            if (secretKey == null) {
                handler.sendStatusResponse(StatusCode.BAD_REQUEST);
                return;
            }

            String websocketKey = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-1").digest(
                            (secretKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)
                    )
            );

            handler.setResponseHeaders("Upgrade", "websocket");
            handler.setResponseHeaders("Connection", "Upgrade");
            handler.setResponseHeaders("Sec-Websocket-Accept", websocketKey);

            handler.sendStatusResponse(StatusCode.SWITCHING_PROTOCOLS);

            websocketHandler.handle(handler.inputStream(), handler.outputStream());
        });
    }
}
