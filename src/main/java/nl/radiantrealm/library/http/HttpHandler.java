package nl.radiantrealm.library.http;

public interface HttpHandler {
    void handle(HttpRequest request) throws Exception;
}
