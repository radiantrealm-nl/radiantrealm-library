package nl.radiantrealm.library.http.websocket;

import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.http.WsopCode;
import nl.radiantrealm.library.utils.ByteUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class WebsocketHandler implements ApplicationService {
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Thread thread;
    private boolean isOpen;

    public WebsocketHandler(HttpExchange exchange) {
        WebsocketSession session = new WebsocketSession(exchange);
        this.inputStream = session.inputStream();
        this.outputStream = session.outputStream();
        this.isOpen = false;

        thread = new Thread(() -> {
            try {
                while (isOpen) {
                    if (inputStream.available() < 1) {
                        Thread.sleep(10);
                        continue;
                    }

                    List<WebsocketFrame> websocketFrameList = new ArrayList<>();

                    for (int i = 0; i < 2; i++) {
                        WebsocketFrame websocketFrame = readFrame();

                        if (!websocketFrame.finalMessage()) {
                            i--;
                        }

                        websocketFrameList.add(websocketFrame);
                    }

                    WebsocketFrame websocketFrame = WebsocketFrame.merge(websocketFrameList.toArray(new WebsocketFrame[0]));
                    onFrame(websocketFrame);
                }
            } catch (Exception e) {
                //Grote oei, log of onFrame met lege socket?
            }
        });
    }

    protected abstract void onFrame(WebsocketFrame websocketFrame);

    @Override
    public void start() throws Exception {
        this.isOpen = true;
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        this.isOpen = false;
    }

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
