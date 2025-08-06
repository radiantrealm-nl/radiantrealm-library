package nl.radiantrealm.library.server;

public interface RequestHandler {
    void handle(HttpRequest request) throws Exception;
}
