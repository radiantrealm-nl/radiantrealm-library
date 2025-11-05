package nl.radiantrealm.library.http.websocket;

import java.util.HashMap;
import java.util.Map;

public enum WebsocketOperatorCode {
    CONTINUE(0x00),
    UTF_8(0x01),
    BIINARY(0x02),
    CLOSE(0x08),
    PING(0x09),
    PONG(0x0A);

    private static final Map<Integer, WebsocketOperatorCode> map = new HashMap<>();

    static {
        for (WebsocketOperatorCode operatorCode : WebsocketOperatorCode.values()) {
            map.put(operatorCode.code, operatorCode);
        }
    }

    public static WebsocketOperatorCode getWsopCode(int code) {
        return map.get(code);
    }

    public final int code;

    WebsocketOperatorCode(int code) {
        this.code = code;
    }
}
