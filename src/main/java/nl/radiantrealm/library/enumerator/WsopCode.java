package nl.radiantrealm.library.enumerator;

public enum WsopCode {
    CONTINUATION(0x0, true),
    TEXT_UTF8(0x1, false),
    BINARY(0x2, false),
    MIME_TYPE(0x3, false),
    CLOSE(0x8, true),
    PING(0x9, true),
    PONG(0xA, true);

    private final int code;
    private final boolean hasBody;

    WsopCode(int code, boolean hasBody) {
        this.code = code;
        this.hasBody = hasBody;
    }

    public int getCode() {
        return code;
    }

    public boolean hasBody() {
        return hasBody;
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
