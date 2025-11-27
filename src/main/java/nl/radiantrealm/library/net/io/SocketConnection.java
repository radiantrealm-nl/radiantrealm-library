package nl.radiantrealm.library.net.io;

import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SocketConnection implements AutoCloseable {
    public final VirtualByteBuffer inboundBuffer = new VirtualByteBuffer();
    public final VirtualByteBuffer outboundBuffer = new VirtualByteBuffer();
    public final AtomicBoolean awaitClosing = new AtomicBoolean(false);

    private final SelectorEngine engine;
    public final SelectionKey key;
    public final SocketChannel channel;

    public SocketConnection(SelectorEngine engine, SelectionKey key, SocketChannel channel) {
        this.engine = engine;
        this.key = key;
        this.channel = channel;
    }

    @Override
    public void close() {
        awaitClosing.set(true);
    }

    public void close(boolean force) throws IOException {
        if (force) {
            key.cancel();
            channel.close();
        } else {
            awaitClosing.set(true);
        }
    }

    public void addInboundBuffer(ByteBuffer buffer) {
        synchronized (inboundBuffer) {
            inboundBuffer.add(buffer);
        }
    }

    public void addOutboundBuffer(ByteBuffer buffer) {
        synchronized (outboundBuffer) {
            outboundBuffer.add(buffer);
        }
    }

    public void wakeup() {
        key.selector().wakeup();
    }

    public void enableInterestOp(InterestOp op) {
        engine.addKeyAction(new KeyAction(
                key, op, true
        ));
    }

    public void disableInterestOp(InterestOp op) {
        engine.addKeyAction(new KeyAction(
                key, op, false
        ));
    }
}
