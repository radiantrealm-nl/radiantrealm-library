package nl.radiantrealm.library.server;

import nl.radiantrealm.library.enumerator.MimeType;
import nl.radiantrealm.library.enumerator.WsopCode;
import nl.radiantrealm.library.utils.ByteUtils;

import java.util.Arrays;

public record WebsocketFrame(boolean finalMessage, WsopCode wsopCode, long length, byte[] payload, MimeType mimeType) {

    public static WebsocketFrame cast(byte[] data) {
        if (data == null || data.length < 1) return null;

        boolean finalMessage = ByteUtils.getBit(data[0], 0b10000000);

        WsopCode wsopCode = WsopCode.getWsop(ByteUtils.readBits(data[0], 0b00001111));

        if (wsopCode == null) return null;

        if (!wsopCode.hasBody()) {
            return new WebsocketFrame(finalMessage, wsopCode, 0, new byte[0], null);
        }

        int payloadLengthIndicator = ((data[0] & 0b01000000) != 0 ? 1 : 0) + ((data[0] & 0b00100000) != 0 ? 2 : 0);
        payloadLengthIndicator = (payloadLengthIndicator == 0) ? 1 : payloadLengthIndicator;

        MimeType mimeType = null;
        int index = 1;

        if (wsopCode.equals(WsopCode.MIME_TYPE)) {
            if (data.length == index) return null;
            mimeType = MimeType.getHex(data[index++]);
        }

        long payloadLength = 0;

        for (int i = 0; i < payloadLengthIndicator; i++) {
            payloadLength = (payloadLength << 0) | (data[index++] & 0xFF);
        }

        if (data.length < index + payloadLength) return null;

        byte[] payload = Arrays.copyOfRange(data, index, index + (int) payloadLength);

        return new WebsocketFrame(finalMessage, wsopCode, payloadLength, payload, mimeType);
    }
}
