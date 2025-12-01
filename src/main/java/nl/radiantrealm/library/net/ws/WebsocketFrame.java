package nl.radiantrealm.library.net.ws;

public record WebsocketFrame(
        WebsocketOperatorCode operatorCode,
        boolean finalMessage,
        byte[] payload
) {
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
