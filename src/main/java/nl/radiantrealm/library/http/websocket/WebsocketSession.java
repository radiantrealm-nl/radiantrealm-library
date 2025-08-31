package nl.radiantrealm.library.http.websocket;

import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.http.WsopCode;
import nl.radiantrealm.library.utils.ByteUtils;
import nl.radiantrealm.library.utils.Logger;
import nl.radiantrealm.library.utils.Result;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class WebsocketSession implements WebsocketHandler, ApplicationService {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final InputStream inputStream;
    protected final OutputStream outputStream;
    protected final ExecutorService executorService;

    public WebsocketSession(HttpExchange exchange) {
        this.inputStream = exchange.getRequestBody();
        this.outputStream = exchange.getResponseBody();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        ApplicationService.super.start();
        executorService.submit(() -> {
            try {
                if (inputStream.available() > 0) {
                    Result<WebsocketFrame> frame = Result.tryCatch(this::readFrame);
                    onFrame(frame.getObject());
                }

                Thread.sleep(10);
            } catch (Exception e) {
                logger.error("Failed to check for incoming frames.");
            }
        });
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();
        executorService.shutdown();
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
