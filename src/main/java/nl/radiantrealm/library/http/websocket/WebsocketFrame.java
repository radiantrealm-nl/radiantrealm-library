package nl.radiantrealm.library.http.websocket;

import nl.radiantrealm.library.http.WsopCode;

import java.util.Arrays;
import java.util.Comparator;

public record WebsocketFrame(long timestamp, boolean finalMessage, WsopCode wsopCode, byte[] payload) {

    public static WebsocketFrame pingFrame() {
        return new WebsocketFrame(
                System.currentTimeMillis(),
                true,
                WsopCode.PING,
                null
        );
    }

    public static WebsocketFrame merge(WebsocketFrame... websocketFrames) {
        switch (websocketFrames.length) {
            case 0 -> {
                return null;
            }

            case 1 -> {
                return websocketFrames[0];
            }
        }

        WebsocketFrame[] sortedFrames = Arrays.stream(websocketFrames)
                .sorted(Comparator.comparing(WebsocketFrame::timestamp))
                .toArray(WebsocketFrame[]::new);

        int totalPayloadLength = Arrays.stream(sortedFrames)
                .mapToInt(WebsocketFrame::payloadLength)
                .sum();

        byte[] combinedPayload = new byte[totalPayloadLength];
        int offset = 0;
        for (WebsocketFrame frame : sortedFrames) {
            System.arraycopy(frame.payload, 0, combinedPayload, offset, frame.payloadLength());
            offset += frame.payloadLength();
        }

        WebsocketFrame leadingFrame = sortedFrames[0];

        return new WebsocketFrame(leadingFrame.timestamp, true, leadingFrame.wsopCode, combinedPayload);
    }

    public int payloadLength() {
        return payload.length;
    }
}
