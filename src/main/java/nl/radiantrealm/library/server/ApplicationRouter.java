package nl.radiantrealm.library.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.utils.Logger;

import java.net.InetSocketAddress;

public abstract class ApplicationRouter {
    private static final Logger logger = Logger.getLogger(ApplicationRouter.class);
    private static HttpServer server;

    public ApplicationRouter(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (Exception e) {
            logger.error(String.format("Failed to create HttpServer at %s.", port), e);
        }
    }

    protected void createAPIHandler(String path, HttpHandler handler) {
        server.createContext(path, handler);
    }

    protected void createAPIHandler(String path, RequestHandler handler) {
        createAPIHandler(path, handler::handle);
    }
}
