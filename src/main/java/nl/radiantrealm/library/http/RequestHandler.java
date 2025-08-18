package nl.radiantrealm.library.http;

public interface RequestHandler {
    void handle(HttpRequest request) throws Exception;
}
