package nl.radiantrealm.library.http.websocket;

public class WebsocketException extends RuntimeException {
    public final WebsocketStatusCode statusCode;

    public WebsocketException(WebsocketStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public WebsocketException(Exception e, WebsocketStatusCode statusCode) {
        super(e);
        this.statusCode = statusCode;
    }
}
