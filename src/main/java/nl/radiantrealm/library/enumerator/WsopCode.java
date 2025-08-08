package nl.radiantrealm.library.enumerator;

public enum WsopCode {
    CONTINUATION(0x0),
    TEXT_UTF8(0x1),
    BINARY(0x2),
    CLOSE(0x8),
    PING(0x9),
    PONG(0xA);

    private final int code;

    WsopCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static WsopCode getWsop(int integer) {
        for (WsopCode wsopCode : WsopCode.values()) {
            if (wsopCode.code == integer) {
                return wsopCode;
            }
        }

        return null;
    }
}
