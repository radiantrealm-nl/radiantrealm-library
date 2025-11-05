package nl.radiantrealm.library.http.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record WebsocketFrame(
        WebsocketOperatorCode operatorCode,
        boolean finalMessage,
        byte[] payload
) {
    public byte[] toBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(payload.length);

        int firstByte = (finalMessage ? 0x80 : 0) | (operatorCode().code & 0x0F);
        outputStream.write(firstByte);

        if (payload.length <= 125) {
            outputStream.write(payload.length);
        } else if (payload.length <= 65535) {
            outputStream.write(126);
            outputStream.write((payload.length >> 8) & 0xFF);
            outputStream.write(payload.length & 0xFF);
        } else {
            outputStream.write(127);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(payload.length);
            try {
                outputStream.write(buffer.array());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            outputStream.write(payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }
}
