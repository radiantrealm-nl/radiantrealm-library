package nl.radiantrealm.library.http.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.http.HttpException;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.utils.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ApplicationRouter implements ApplicationService {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final int port;
    protected HttpServer server;
    protected final AtomicInteger handledRequests;

    public ApplicationRouter(int port) {
        this.port = port;
        this.server = null;
        this.handledRequests = new AtomicInteger(0);
    }

    protected HttpServer buildHttpServer() throws Exception {
        return HttpServer.create(new InetSocketAddress(port), 0);
    }

    @Override
    public void start() throws Exception {
        server = buildHttpServer();
        server.start();
        handledRequests.set(0);

        ApplicationService.super.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop(60);
        server = null;

        ApplicationService.super.stop();
    }

    @Override
    public JsonObject status() throws Exception {
        JsonObject object = new JsonObject();
        object.addProperty("port", port);
        return object;
    }

    public void register(HttpHandler handler, String path) {
        server.createContext(path, handler);
    }

    public void register(RequestHandler handler, String path) {
        server.createContext(path, exchange -> {
            try (exchange) {
                HttpRequest request = new HttpRequest(exchange);

                try {
                    handler.handle(request);
                    handledRequests.incrementAndGet();
                } catch (HttpException e) {
                    request.sendStatusResponse(e.response);
                }
            } catch (Exception e) {
                logger.error("Unexpected error while handling request.", e);
            }
        });
    }

    public void register(RequestHandler handler, String... paths) {
        for (String string : paths) {
            register(handler, string);
        }
    }
}
