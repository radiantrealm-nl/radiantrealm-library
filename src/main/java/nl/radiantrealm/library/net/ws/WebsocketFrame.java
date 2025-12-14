package nl.radiantrealm.library.net.ws;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record WebsocketFrame(
        WebsocketOperatorCode operatorCode,
        boolean finalMessage,
        byte[] payload
) {
    public WebsocketFrame(
            WebsocketOperatorCode operatorCode,
            boolean finalMessage,
            byte[] payload
    ) {
        this.operatorCode = Objects.requireNonNull(operatorCode);
        this.finalMessage = finalMessage;
        this.payload = Objects.requireNonNull(payload);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includePayload) {
        int length = 37 + operatorCode.name().length() + (includePayload ? payload.length : 0);
        StringBuilder builder = new StringBuilder(length);
        builder.append(String.format(
                "Operator Code: %s (0x%s)\n",
                operatorCode.name(),
                String.format("%02X", operatorCode.code)
        ));

        builder.append(String.format(
                "Final message: %s\n",
                finalMessage
        ));

        if (includePayload) {
            for (byte b : payload) {
                builder.append(String.format("%02X", b));
            }
        }

        return builder.toString();
    }

    public String toString(Charset charset) {
        String headerString = toString(false);

        int length = headerString.length() + payload.length;
        StringBuilder builder = new StringBuilder(length);
        builder.append(headerString);

        switch (operatorCode) {
            case CONTINUE, TEXT, BINARY -> builder.append(new String(payload, charset));

            case CLOSE -> {
                if (payload.length < 2 || payload.length > 125) {
                    builder.append("Exit code: Invalid");
                    break;
                }

                WebsocketExitCode exitCode = WebsocketExitCode.valueOfCode((payload[0] & 0xFF) << 8 | payload[1] & 0xFF);

                if (exitCode == null) {
                    builder.append("Exit code: Invalid");
                    break;
                }

                byte[] reasonBytes = new byte[payload.length - 2];
                String reasonString = new String(reasonBytes, charset);
                builder.append(String.format("Exit code: %s %s", exitCode.code, reasonString));
            }
        }

        return builder.toString();
    }

    public byte[] getBytes() {
        int frameLength = 2 + payload.length;

        if (payload.length > 125 && payload.length <= 65535) {
            frameLength += 2;
        } else if (payload.length > 65535) {
            frameLength += 8;
        }

        byte[] bytes = new byte[frameLength];
        bytes[0] = (byte) ((finalMessage ? 0x80 : 0) | (operatorCode.code) & 0x0F);

        int index = 1;
        if (payload.length <= 125) {
            bytes[index++] = (byte) payload.length;
        } else if (payload.length <= 65535) {
            bytes[index++] = 126;
            bytes[index++] = (byte) ((payload.length >> 8) & 0xFF);
            bytes[index++] = (byte) (payload.length & 0xFF);
        } else {
            bytes[index++] = 127;

            for (int i = 7; i >= 0; i--) {
                bytes[index++] = (byte) ((payload.length >> (8 * i)) & 0xFF);
            }
        }

        System.arraycopy(payload, 0, bytes, index, payload.length);
        return bytes;
    }
}
