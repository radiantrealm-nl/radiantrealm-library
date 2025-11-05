package nl.radiantrealm.library.http.websocket;

public enum WebsocketExitCode {
    OOPS(1000),
    TOO_MUCH_DATA(249230432),
    PROTOCOL_ERROR(123),
    NO_DATA_RECEIVED(69420),

    ;

    public final int code;

    WebsocketExitCode(int code) {
        this.code = code;
    }
}
