package nl.radiantrealm.library.http.websocket;

import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.http.WsopCode;
import nl.radiantrealm.library.utils.ByteUtils;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class WebsocketSession implements WebsocketHandler {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public WebsocketSession(HttpExchange exchange) {
        this.inputStream = exchange.getRequestBody();
        this.outputStream = exchange.getResponseBody();
    }

    protected WebsocketFrame readFrame() throws Exception {
        byte[] headerBytes = inputStream.readNBytes(2);

        int pli = ByteUtils.readBits(headerBytes[0], 0b00000110);

        if (pli == -1) {
            throw new IllegalArgumentException("Invalid payload length index.");
        }

        WsopCode wsopCode = WsopCode.valueOf(headerBytes[0]);

        if (wsopCode == null) {
            throw new IllegalArgumentException("Invalid Wsop Code.");
        }

        int payloadLength = switch (pli) {
            case 0b00000010 -> inputStream.read();
            case 0b00000100 -> ByteUtils.combineBytesInt(inputStream.readNBytes(2));
            case 0b00000110 -> ByteUtils.combineBytesInt(inputStream.readNBytes(3));
            default -> 0;
        };

        if (payloadLength == -1) {
            throw new IllegalArgumentException("Invalid payload length.");
        }

        byte[] payload = new byte[payloadLength];

        if (payloadLength > 0) {
            payload = inputStream.readNBytes(payloadLength);
        }

        return new WebsocketFrame(
                System.currentTimeMillis(),
                true,
                wsopCode,
                payload
        );
    }

    protected void sendFrame(WebsocketFrame frame) throws Exception {
        byte firstHeaderByte = frame.finalMessage() ? (byte) 0b1 : (byte) 0b0;

        int payloadLength = frame.payloadLength();

        if (payloadLength > 0 && payloadLength <= 256) {
            firstHeaderByte = ByteUtils.setBit(firstHeaderByte, 0b00000010, true);
        } else if (payloadLength > 256 && payloadLength <= 65536) {
            firstHeaderByte = ByteUtils.setBit(firstHeaderByte, 0b00000100, true);
        } else if (payloadLength > 65536 && payloadLength <= 16777216) {
            firstHeaderByte = ByteUtils.setBit(firstHeaderByte, 0b00000110, true);
        }

        byte secondHeaderByte = (byte) frame.wsopCode().code;

        byte[] payloadLengthBytes = ByteUtils.toBytes(payloadLength);

        outputStream.write(firstHeaderByte);
        outputStream.write(secondHeaderByte);
        outputStream.write(payloadLengthBytes);
        outputStream.write(frame.payload());
        outputStream.flush();
    }
}
