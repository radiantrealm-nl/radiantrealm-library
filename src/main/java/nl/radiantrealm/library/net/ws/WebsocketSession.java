package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpConnection;
import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.net.io.SelectorEngine;
import nl.radiantrealm.library.net.io.SocketConnection;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class WebsocketSession extends SocketConnection {
    public final String sessionID = UUID.randomUUID().toString();

    public final FragmentedFrame fragmentedFrame = new FragmentedFrame();
    public final AggregatedWebsocketParser parser = new AggregatedWebsocketParser(this);
    public final AtomicLong lastActivityMillis = new AtomicLong(System.currentTimeMillis());
    public final AtomicBoolean acknowledgedClosing = new AtomicBoolean(false);

    public WebsocketSession(SelectorEngine engine, SocketChannel channel) {
        super(engine, channel);
    }

    public static WebsocketSession upgrade(SelectorEngine engine, HttpConnection connection) {
        WebsocketSession session = new WebsocketSession(
                engine,
                connection.channel
        );

        synchronized (connection.inboundBuffer) {
            connection.inboundBuffer.duplicate(session.inboundBuffer);
        }

        synchronized (connection.outboundBuffer) {
            connection.outboundBuffer.duplicate(session.outboundBuffer);
        }

        return session;
    }

    public void sendFrame(WebsocketFrame frame) {
        addOutboundBuffer(frame.getBytes());
        enableInterestOp(InterestOp.OP_WRITE);
        wakeup();
    }

    public void sendText(String string) {
        sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.TEXT,
                true,
                string.getBytes(StandardCharsets.UTF_8)
        ));
    }

    public void sendBinary(byte[] bytes) {
        sendFrame(new WebsocketFrame(
                WebsocketOperatorCode.BINARY,
                true,
                bytes
        ));
    }
}
