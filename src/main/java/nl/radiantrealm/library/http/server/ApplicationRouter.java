package nl.radiantrealm.library.http.server;

import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.utils.Logger;

import java.net.InetSocketAddress;

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
}
