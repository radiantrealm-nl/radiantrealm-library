package nl.radiantrealm.library.net.ws;

public class WebsocketException extends RuntimeException {
    public final WebsocketFrame frame;

    public WebsocketException(WebsocketFrame frame) {
        this.frame = frame;
    }

    public WebsocketException(Exception e, WebsocketFrame frame) {
        super(e);

        this.frame = frame;
    }
}
