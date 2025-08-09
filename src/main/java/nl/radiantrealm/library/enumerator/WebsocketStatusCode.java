package nl.radiantrealm.library.enumerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum WebsocketStatusCode {
    NORMAL_CLOSURE(1000, true),
    GOING_AWAY(1001, true),
    PROTOCOL_ERROR(1002, true),
    UNSUPPORTED_DATA(1003, true),

    NO_CODE_RECEIVED(1005, false),
    CLOSED_ABNORMALLY(1006, false),

    INVALID_PAYLOAD(1007, true),
    POLICY_VIOLATED(1008, true),
    MESSAGE_OVERFLOW(1009, true),
    UNSUPPORTED_EXTENSION(1010, true),
    SERVER_ERROR(1011, true),

    TLS_FAILURE(1015, false);

    private final int code;
    private final boolean isCloseFrame;

    private static final Map<Integer, WebsocketStatusCode> map = new HashMap<>();

    static {
        Arrays.asList(WebsocketStatusCode.values()).forEach(key -> {
            map.put(key.code, key);
        });
    }

    WebsocketStatusCode(int code, boolean isCloseFrame) {
        this.code = code;
        this.isCloseFrame = isCloseFrame;
    }

    public int getCode() {
        return code;
    }

    public boolean isCloseFrame() {
        return isCloseFrame;
    }

    public static WebsocketStatusCode getCode(int code) {
        return map.get(code);
    }
}
