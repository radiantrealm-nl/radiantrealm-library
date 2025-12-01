package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpConnection;
import nl.radiantrealm.library.net.io.SelectorEngine;
import nl.radiantrealm.library.net.io.SocketConnection;
import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class WebsocketSession extends SocketConnection {
    public final String sessionID = UUID.randomUUID().toString();

    public final Queue<WebsocketFrame> fragmentedFrames = new ConcurrentLinkedQueue<>();
    public final AggregatedWebsocketFrame aggregatedWebsocketFrame = new AggregatedWebsocketFrame(this);
    public final AtomicLong lastActivityMillis = new AtomicLong(System.currentTimeMillis());
    public final AtomicBoolean acknowledgedClosing = new AtomicBoolean(false);

    public WebsocketSession(SelectorEngine engine, SelectionKey key, SocketChannel channel) {
        super(engine, key, channel);
    }

    public static WebsocketSession upgrade(HttpConnection connection, SelectorEngine engine, SelectionKey key) {
        WebsocketSession session = new WebsocketSession(engine, key, connection.channel);

        VirtualByteBuffer inboundBuffer = connection.inboundBuffer;
        synchronized (inboundBuffer) {
            byte[] bytes = inboundBuffer.peek(inboundBuffer.size());
            session.inboundBuffer.add(bytes);
        }

        VirtualByteBuffer outboundBuffer = connection.outboundBuffer;
        synchronized (outboundBuffer) {
            byte[] bytes = outboundBuffer.peek(outboundBuffer.size());
            session.outboundBuffer.add(bytes);
        }

        return session;
    }
}
