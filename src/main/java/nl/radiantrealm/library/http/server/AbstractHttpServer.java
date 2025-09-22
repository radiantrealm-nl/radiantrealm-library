package nl.radiantrealm.library.http.server;

import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.http.*;
import nl.radiantrealm.library.utils.Logger;

import java.net.InetSocketAddress;

public abstract class AbstractHttpServer {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final HttpServer server;
    protected final int port;

    public AbstractHttpServer(int port) {
        this.server = createHttpServer();
        this.port = port;

        if (server == null) {
            logger.error(String.format("Cannot to start HTTP server on port '%s'. HTTP server is not initialized.", port));
        } else {
            server.start();
        }
    }

    protected HttpServer createHttpServer() {
        try {
            return HttpServer.create(new InetSocketAddress(port), 0);
        } catch (Exception e) {
            logger.error(String.format("Failed to create HTTP server at port '%s'.", port));
            return null;
        }
    }

    protected void register(HttpHandler handler, String path) {
        if (server == null) {
            logger.error(String.format("Cannot register handler at '%s' for port '%s'. HTTP server is not initialized.", path, port));
            return;
        }

        server.createContext(path, exchange -> {
            try {
                HttpRequest request = new HttpRequest(exchange);

                try {
                    handler.handle(request);
                } catch (HttpException e) {
                    HttpResponse response = e.response;

                    if (response == null) {
                        request.sendStatusResponse(StatusCode.BAD_REQUEST, "No context.");
                    } else {
                        request.sendResponse(
                                response.statusCode(),
                                response.mediaType(),
                                response.responseBody()
                        );
                    }
                } catch (Exception e) {
                    logger.error(String.format("Unexpected error while handling request at '%s' for port '%s'.", path, port), e);
                    request.sendStatusResponse(StatusCode.SERVER_ERROR);
                }
            } catch (Exception e) {
                logger.error(String.format("Failed to send response at '%s' for port '%s'.", path, port), e);
            }
        });
    }

    protected void register(HttpHandler handler, String... paths) {
        for (String string : paths) {
            register(handler, string);
        }
    }
}
