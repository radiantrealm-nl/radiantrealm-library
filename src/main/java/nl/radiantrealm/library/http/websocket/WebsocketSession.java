package nl.radiantrealm.library.http.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class WebsocketSession {
    private final List<WebsocketFrame> fragmentedFrames = new ArrayList<>();
    private final ByteArrayOutputStream outputStream;

    public final String sessionID;
    public final SocketChannel channel;

    public WebsocketSession(WebsocketConfiguration configuration, String sessionID, SocketChannel channel) {
        this.outputStream = new ByteArrayOutputStream(configuration.sessionBufferSize());
        this.sessionID = sessionID;
        this.channel = channel;
    }

    public synchronized void addFragmentedFrame(WebsocketFrame frame) {
        fragmentedFrames.add(frame);
    }

    public synchronized List<WebsocketFrame> consumeFragmentedFrames() {
        List<WebsocketFrame> list = new ArrayList<>(fragmentedFrames);
        fragmentedFrames.clear();
        return list;
    }

    public synchronized void appendInputBytes(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public synchronized byte[] getInputBytes() {
        return outputStream.toByteArray();
    }

    public synchronized byte[] consumeInputBytes(int length) throws IOException {
        byte[] original = outputStream.toByteArray();
        if (length > original.length) {
            length = original.length;
        }
        byte[] consumed = new byte[length];
        System.arraycopy(original, 0, consumed, 0, length);

        outputStream.reset();
        outputStream.write(original, length, original.length - length);
        return consumed;
    }
}
