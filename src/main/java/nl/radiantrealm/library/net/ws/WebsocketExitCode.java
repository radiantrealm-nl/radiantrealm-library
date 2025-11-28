package nl.radiantrealm.library.net.ws;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public enum WebsocketExitCode {
    NORMAL_CLOSURE(1000, "Normal closure"),
            GOING_AWAY(1001, "Going away"),
            PROTOCOL_ERROR(1002, "Protocol error"),
            UNSUPPORTED_DATA(1003, "Unsupported data"),
            NO_CODE_RECEIVED(1005, "No code received"),
            CONNECTION_CLOSED_ABNORMALLY(1006, "Connection closed abnormally"),
            INVALID_PAYLOAD_DATA(1007, "Invalid payload data"),
            POLICY_VIOLATED(1008, "Policy violated"),
            MESSAGE_TOO_BIG(1009, "Message too big"),
            UNSUPPORTED_EXTENSION(1010, "Unsupported extension"),
            INTERNAL_SERVER_ERROR(1011, "Internal server error"),
            TLS_HANDSHAKE_FAILURE(1015, "TLS handshake failure");

    private static final Map<Integer, WebsocketExitCode> map = new HashMap<>();

    static {
        for (WebsocketExitCode exitCode : WebsocketExitCode.values()) {
            map.put(exitCode.code, exitCode);
        }
    }

    public static WebsocketExitCode valueOfCode(int code) {
        return map.get(code);
    }

    public final int code;
    public final String message;

    WebsocketExitCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public WebsocketFrame generateFrame() {
        return generateFrame(message);
    }

    public WebsocketFrame generateFrame(@NotNull String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int messageLength = Math.min(messageBytes.length, 123);
        byte[] bytes = new byte[2 + messageLength];
        bytes[0] = (byte) ((code >> 8) & 0xFF);
        bytes[1] = (byte) (code & 0xFF);
        System.arraycopy(messageBytes, 0, bytes, 2, messageLength);

        return new WebsocketFrame(
                WebsocketOperatorCode.CLOSE,
                true,
                bytes
        );
    }
}
