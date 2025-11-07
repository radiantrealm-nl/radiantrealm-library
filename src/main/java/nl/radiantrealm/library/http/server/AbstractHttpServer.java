package nl.radiantrealm.library.http.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.http.model.HttpException;
import nl.radiantrealm.library.http.model.HttpTools;
import nl.radiantrealm.library.utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class AbstractHttpServer implements AutoCloseable {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final HttpServer server;
    protected final int port;

    public AbstractHttpServer(int port) throws IOException {
        this.server = createHttpServer(port);
        this.port = port;
    }

    @Override
    public void close() {
        server.stop(0);
    }

    public void start() {
        server.start();
    }

    protected HttpServer createHttpServer(int port) throws IOException {
        return HttpServer.create(new InetSocketAddress(port), 0);
    }

    protected void register(HttpHandler handler, boolean keepAlive, String path) {
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
