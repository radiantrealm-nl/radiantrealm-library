package nl.radiantrealm.library.http.websocket;

public class WebsocketException extends RuntimeException {
    public final WebsocketExitCode exitCode;

    public WebsocketException(WebsocketExitCode exitCode) {
        this.exitCode = exitCode;
    }

    public WebsocketException(Exception e, WebsocketExitCode exitCode) {
        super(e);
        this.exitCode = exitCode;
    }
}
