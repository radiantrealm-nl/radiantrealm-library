package nl.radiantrealm.library.net.http;

import java.io.IOException;

public interface HttpHandler {
    void handle(HttpExchange exchange) throws IOException;
}
