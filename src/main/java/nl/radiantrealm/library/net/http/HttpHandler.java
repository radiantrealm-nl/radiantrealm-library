package nl.radiantrealm.library.net.http;

public interface HttpHandler {
    void handle(HttpConnection connection, HttpRequest request);
}
