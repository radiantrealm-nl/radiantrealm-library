package nl.radiantrealm.library.http.handler;

import nl.radiantrealm.library.http.model.HttpRequest;

public interface HttpHandler {
    void handle(HttpRequest request) throws Exception;
}
