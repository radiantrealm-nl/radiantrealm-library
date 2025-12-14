package nl.radiantrealm.library.net.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public abstract class SocketEngine extends SelectorEngine {
    protected final SocketConfiguration configuration;

    public SocketEngine(SocketConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
    }

    @Override
    protected void handleRead(SelectionKey key) {
        if (key.attachment() instanceof SocketConnection connection) {
            ByteBuffer buffer = ByteBuffer.allocate(configuration.incomingBufferSize());

            try {
                int read = connection.channel.read(buffer);

                if (read == -1) {
                    connection.silentClose();
                    onDisconnect(key, connection);
                    return;
                }

                if (read == 0) {
                    return;
                }

                connection.addInboundBuffer(buffer.flip());
            } catch (IOException e) {
                logger.error("Exception while reading channel", e);
                connection.silentClose();
                onDisconnect(key, connection);
                return;
            }

            onRead(key, connection);
        }
    }

    @Override
    protected void handleWrite(SelectionKey key) {
        if (key.attachment() instanceof SocketConnection connection) {
            VirtualByteBuffer outboundBuffer = connection.outboundBuffer;

            try {
                synchronized (outboundBuffer) {
                    while (!outboundBuffer.isEmpty()) {
                        ByteBuffer buffer = ByteBuffer.wrap(outboundBuffer.read(configuration.outgoingBufferSize()));
                        outboundBuffer.remove(connection.channel.write(buffer));

                        if (buffer.hasRemaining()) {
                            return;
                        }
                    }
                }

                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            } catch (IOException e) {
                logger.error("Exception while writing channel", e);
                connection.silentClose();
                onDisconnect(key, connection);
                return;
            }

            onWrite(key, connection);
        }
    }

    protected void onRead(SelectionKey key, SocketConnection socketConnection) {}
    protected void onWrite(SelectionKey key, SocketConnection socketConnection) {}
    protected void onDisconnect(SelectionKey key, SocketConnection socketConnection) {}
}
