package nl.radiantrealm.library.server;

import nl.radiantrealm.library.enumerator.MimeType;
import nl.radiantrealm.library.enumerator.WebsocketStatusCode;
import nl.radiantrealm.library.enumerator.WsopCode;
import nl.radiantrealm.library.utils.ByteUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.Arrays;

public record WebsocketFrame(boolean finalMessage, WsopCode wsopCode, byte[] payload, MimeType mimeType, WebsocketStatusCode websocketStatusCode) {

    public static Result<WebsocketFrame> cast(byte[] data) {
        if (data == null || data.length == 0) {
            return Result.error(new IllegalArgumentException("Frame must contain at least one byte."));
        }

        boolean finalMessage = ByteUtils.getBit(data[0], 0b10000000);

        WsopCode wsopCode = WsopCode.getWsop(ByteUtils.readBits(data[0], 0b00001111));

        if (wsopCode == null) {
            return Result.error(new IllegalArgumentException("Invalid WSOP Code."));
        }

        if (!wsopCode.hasBody()) {
            return Result.ok(new WebsocketFrame(finalMessage, wsopCode, new byte[0], null, null));
        }

        if (wsopCode.equals(WsopCode.CLOSE)) {
            WebsocketStatusCode statusCode = WebsocketStatusCode.getCode(ByteUtils.combineBytes(data[1], data[2]));
            return Result.ok(new WebsocketFrame(finalMessage, wsopCode, new byte[0], null, statusCode));
        }

        int pli = switch (ByteUtils.readBits(data[0], 0b01100000)) {
            case 0b000 -> 1;
            case 0b010 -> 2;
            case 0b100 -> 3;
            case 0b110 -> 4;

            default -> 0;
        };

        int payloadIndex = pli + 1;

        MimeType mimeType = switch (wsopCode) {
            case MIME_TYPE -> {
                payloadIndex++;
                yield MimeType.getHex(data[1]);
            }

            default -> null;
        };

        if (wsopCode.equals(WsopCode.MIME_TYPE) && mimeType == null) {
            return Result.error(new IllegalArgumentException("Invalid MIME type."));
        }

        if (payloadIndex > data.length) {
            return Result.error(new IllegalArgumentException("Frame too short for declared payload fields."));
        }

        byte[] remaining = Arrays.copyOfRange(data, payloadIndex, data.length);

        return Result.ok(new WebsocketFrame(finalMessage, wsopCode, remaining, mimeType, null));
    }
}
