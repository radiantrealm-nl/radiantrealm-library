package nl.radiantrealm.library.server;

import com.sun.net.httpserver.HttpExchange;

public interface ResponseHandler {
    void handle(HttpExchange exchange) throws Exception;
}
