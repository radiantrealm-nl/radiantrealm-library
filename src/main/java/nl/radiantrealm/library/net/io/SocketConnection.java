package nl.radiantrealm.library.net.io;

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
    public final SocketChannel channel;
    public final SelectionKey key;

    public SocketConnection(SelectorEngine engine, SocketChannel channel) {
        this.engine = engine;
        this.channel = channel;
        this.key = channel.keyFor(engine.selector);
    }

    @Override
    public void close() {
        awaitClosing.set(true);
    }

    public void silentClose() {
        try {
            key.cancel();
            channel.close();
        } catch (IOException ignored) {}
    }

    public void wakeup() {
        engine.selector.wakeup();
    }

    public void addInboundBuffer(byte[] bytes) {
        synchronized (inboundBuffer) {
            inboundBuffer.add(bytes);
        }
    }

    public void addInboundBuffer(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        addInboundBuffer(bytes);
    }

    public void addOutboundBuffer(byte[] bytes) {
        synchronized (outboundBuffer) {
            outboundBuffer.add(bytes);
        }
    }

    public void addOutboundBuffer(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        addOutboundBuffer(bytes);
    }

    public synchronized void enableInterestOp(InterestOp op) {
        engine.addKeyAction(new SelectorEngine.KeyAction(
                key, op, true
        ));
    }

    public synchronized void disableInterestOp(InterestOp op) {
        engine.addKeyAction(new SelectorEngine.KeyAction(
                key, op, false
        ));
    }
}
