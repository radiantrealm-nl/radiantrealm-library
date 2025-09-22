package nl.radiantrealm.library.http;

import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.utils.Logger;

import java.net.InetSocketAddress;

public abstract class HttpRouter {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final HttpServer server;

    public HttpRouter(int port) {
        this.server = createHttpServer(port);

        if (server == null) {
            logger.error(String.format("Unable to start HTTP server at port '%s'.", port));
        } else {
            server.start();
        }
    }

    protected HttpServer createHttpServer(int port) {
        try {
            return HttpServer.create(new InetSocketAddress(port), 0);
        } catch (Exception e) {
            logger.error(String.format("Failed to create HTTP server at port '%s'.", port), e);
            return null;
        }
    }

    protected void register(HttpHandler handler, String path) {
        server.createContext(path, exchange -> {
            try {
                HttpRequest request = new HttpRequest(exchange);

                try {
                    handler.handle(request);
                } catch (HttpException e) {
                    request.sendResponse(e.response);
                } catch (Exception e) {
                    logger.error(String.format("Unexpected error in '%s'.", path), e);
                    request.sendStatusResponse(StatusCode.SERVER_ERROR);
                }
            } catch (Exception e) {
                logger.error(String.format("Failed to send response in '%s'.", path), e);
            }
        });
    }

    protected void register(HttpHandler handler, String... paths) {
        for (String string : paths) {
            register(handler, string);
        }
    }
}
