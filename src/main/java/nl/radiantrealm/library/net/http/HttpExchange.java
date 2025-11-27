package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public record HttpExchange(
        HttpConnection connection,
        HttpRequest request
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        connection.close();
    }

    public void sendResponse(HttpResponse response) {
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        VirtualByteBuffer outboundBuffer = connection.outboundBuffer;

        synchronized (outboundBuffer) {
            outboundBuffer.add(buffer);
        }

        connection.enableInterestOp(InterestOp.OP_WRITE);
        connection.wakeup();
    }
}
