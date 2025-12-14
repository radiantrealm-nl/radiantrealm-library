package nl.radiantrealm.library.net.ws;

import java.util.Objects;

public class WebsocketException extends RuntimeException {
    public final WebsocketFrame frame;

    public WebsocketException(WebsocketFrame frame) {
        this.frame = Objects.requireNonNull(frame);
    }

    public WebsocketException(RuntimeException e, WebsocketFrame frame) {
        super(e);
        this.frame = Objects.requireNonNull(frame);
    }
}
