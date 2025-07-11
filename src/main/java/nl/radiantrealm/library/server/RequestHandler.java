package nl.radiantrealm.library.server;

import com.sun.net.httpserver.HttpExchange;

public interface RequestHandler {
    void handle(HttpExchange exchange) throws Exception;
}
