package nl.radiantrealm.library.http.websocket;

import nl.radiantrealm.library.http.enumerator.WsopCode;

import java.util.Arrays;

public record WebsocketFrame(
        WsopCode wsopCode,
        boolean finalMessage,
        byte[] payload
) {

    public WebsocketFrame(WsopCode wsopCode, byte[] payload) {
        this(
                wsopCode,
                true,
                payload
        );
    }

    public int payloadLength() {
        return payload.length;
    }

    public static WebsocketFrame fromBytes(byte[] bytes) {
        return fromBytes(WebsocketConfiguration.defaultConfiguration, bytes);
    }

    public static WebsocketFrame fromBytes(WebsocketConfiguration configuration, byte[] bytes) {
        if (bytes.length < 2) {
            throw new WebsocketException(WebsocketStatusCode.INVALID_PAYLOAD_DATA);
        }

        WsopCode wsopCode = WsopCode.getWsopCode(bytes[0] & 0xFF);

        if (wsopCode == null) {
            throw new WebsocketException(WebsocketStatusCode.INVALID_WSOP_CODE);
        }

        if (!configuration.allowFragmentation() && wsopCode.equals(WsopCode.CONTINUE)) {
            throw new WebsocketException(WebsocketStatusCode.PROTOCOL_ERROR);
        }

        byte configByte = bytes[1];
        boolean finalMessage = (configByte & 0x01) == 0x01;
        boolean isMasked = (configByte & 0x02) == 0x02;

        if (configuration.enforceBitMasking() && !isMasked) {
            throw new WebsocketException(WebsocketStatusCode.PROTOCOL_ERROR);
        }

        int payloadLengthIndex = ((configByte & 0xFF) & 0x0C) >>> 2;
        byte[] payload = switch (payloadLengthIndex) {
            case 0 -> new byte[0];

            case 1, 2, 3 -> {
                if (bytes.length <= (isMasked ? 6 : 2) + payloadLengthIndex) {
                    throw new WebsocketException(WebsocketStatusCode.INVALID_PAYLOAD_DATA);
                }

                byte[] payloadLengthBytes = Arrays.copyOfRange(bytes, isMasked ? 6 : 2, (isMasked ? 6 : 2) + payloadLengthIndex);
                int payloadLength = 0;
                for (int i = 0; i < payloadLengthIndex; i++) {
                    payloadLength |= (payloadLengthBytes[i] & 0xFF) << (8 * (payloadLengthIndex - 1 - i));
                }

                if (bytes.length < (isMasked ? 6 : 2) + payloadLengthIndex + payloadLength) {
                    throw new WebsocketException(WebsocketStatusCode.INVALID_PAYLOAD_DATA);
                }

                if (payloadLength > configuration.maxPayloadLength()) {
                    throw new WebsocketException(WebsocketStatusCode.MESSAGE_TOO_BIG);
                }

                int payloadBeginIndex = (isMasked ? 6 : 2) + payloadLengthIndex;
                yield Arrays.copyOfRange(bytes, payloadBeginIndex, payloadBeginIndex + payloadLength);
            }

            default -> throw new WebsocketException(WebsocketStatusCode.MESSAGE_TOO_BIG);
        };

        if (isMasked && payload.length > 0) {
            byte[] maskingKey = Arrays.copyOfRange(bytes, 2 + payloadLengthIndex, 5 + payloadLengthIndex);
            byte[] unmaskedPayload = new byte[payload.length];

            for (int i = 0; i < payload.length; i++) {
                unmaskedPayload[i] = (byte) (payload[i] ^ maskingKey[i & 4]);
            }

            payload = unmaskedPayload;
        }

        return new WebsocketFrame(
                wsopCode,
                finalMessage,
                payload
        );
    }

    public byte[] toBytes() {
        if (payload.length > 16777217) {
            throw new WebsocketException(WebsocketStatusCode.MESSAGE_TOO_BIG);
        }

        int payloadLengthIndex = payload.length > 65536 ? 3 : payload.length > 256 ? 2 : 1;
        int headerSize = 2 + payloadLengthIndex;

        byte[] bytes = new byte[headerSize + payload.length];
        bytes[0] = (byte) wsopCode.code;
        bytes[1] = (byte) ((finalMessage ? 1 : 0) + (payloadLengthIndex << 2));

        for (int i = 0; i < payloadLengthIndex; i++) {
            bytes[2 + i] = (byte) ((payload.length >> (8 * (payloadLengthIndex - 1 - i))) & 0xFF);
        }

        System.arraycopy(payload, 0, bytes, headerSize, payload.length);
        return bytes;
    }
}
