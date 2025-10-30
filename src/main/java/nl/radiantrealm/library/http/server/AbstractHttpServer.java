package nl.radiantrealm.library.http.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.http.model.HttpException;
import nl.radiantrealm.library.http.model.HttpTools;
import nl.radiantrealm.library.utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class AbstractHttpServer {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final HttpServer server;
    protected final int port;

    public AbstractHttpServer(int port) {
        this.server = createHttpServer();
        this.port = port;

        if (server == null) {
            logger.error(String.format("Cannot start HTTP server on port '%s'. HTTP server is not initialized.", port));
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

    protected void register(HttpHandler handler, boolean keepAlive, String path) {
        if (server == null) {
            throw new IllegalStateException(String.format("Cannot register HTTP handler at path '%s' on port '%s'. HTTP server is not initialized.", path, port));
        }

        server.createContext(path, exchange -> {
            HttpTools tools = new HttpTools(exchange);

            try {
                try {
                    handler.handle(exchange);
                } catch (HttpException e) {
                    tools.sendResponse(e.response);
                } catch (IOException e) {
                    logger.error(String.format("Unexpected error while handling request at path '%s' on port '%s'.", path, port));
                    tools.sendStatusResponse(StatusCode.SERVER_ERROR);
                } finally {
                    if (!keepAlive) {
                        exchange.close();
                    }
                }
            } catch (Exception e) {
                logger.error(String.format("Failed to send response at path '%s' on port '%s'.", path, port));
            }
        });
    }

    protected void register(HttpHandler handler, boolean keepAlive, String... paths) {
        for (String string : paths) {
            register(handler, keepAlive, string);
        }
    }
}
