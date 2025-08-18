package nl.radiantrealm.library.http.websocket;

import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.http.WsopCode;
import nl.radiantrealm.library.utils.ByteUtils;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class WebsocketHandler {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public WebsocketHandler(HttpExchange exchange) {
        WebsocketSession session = new WebsocketSession(exchange);
        this.inputStream = session.inputStream();
        this.outputStream = session.outputStream();
    }

    protected abstract void onFrame(WebsocketFrame websocketFrame);

    protected WebsocketFrame readFrame() throws Exception {
        byte[] headerBytes = inputStream.readNBytes(2);

        boolean finalMessage = ByteUtils.getBit(headerBytes[0], 0b00000001);
        int pli = ByteUtils.readBits(headerBytes[0], 0b00000110);
        WsopCode wsopCode = WsopCode.valueOf(headerBytes[1]);

        if (wsopCode == null) {
            throw new IllegalArgumentException("Invalid Wsop Code.");
        }

        byte[] payloadLength = switch (pli) {
            case 0b00000010 -> inputStream.readNBytes(1);
            case 0b00000100 -> inputStream.readNBytes(2);
            case 0b00000110 -> inputStream.readNBytes(3);
            default -> new byte[0];
        };

        byte[] payload = inputStream.readNBytes((int) ByteUtils.combineBytesLong(payloadLength));

        return new WebsocketFrame(System.currentTimeMillis(), finalMessage, wsopCode, payload);
    }

    protected void sendFrame(WebsocketFrame websocketFrame) throws Exception {
        int pli = definePayloadLengthIndex(websocketFrame.payloadLength());

        byte[] headerBytes = new byte[] {
                (byte) ((websocketFrame.finalMessage() ? 1 : 0) + pli),
                (byte) websocketFrame.wsopCode().code
        };

        byte[] payloadLength = ByteUtils.toBytes(websocketFrame.payloadLength());

        outputStream.write(headerBytes);
        outputStream.write(payloadLength);
        outputStream.write(websocketFrame.payload());
        outputStream.flush();
    }

    private int definePayloadLengthIndex(int payloadLength) {
        if (payloadLength == 0) return 0;
        if (payloadLength <= 256) return 2;
        if (payloadLength <= 65536) return 4;
        if (payloadLength <= 16777216) return 6;
        return 0;
    }
}
