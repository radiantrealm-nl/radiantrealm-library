package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.net.io.SelectorEngine;
import nl.radiantrealm.library.net.io.SocketConnection;

import java.nio.channels.SocketChannel;

public class HttpConnection extends SocketConnection {
    public final AggregatedHttpParser parser = new AggregatedHttpParser(this);

    public HttpConnection(SelectorEngine engine, SocketChannel channel) {
        super(engine, channel);
    }

    public void sendRequest(HttpRequest request) {
        addOutboundBuffer(request.getBytes());
        enableInterestOp(InterestOp.OP_WRITE);
        wakeup();
    }

    public void sendResponse(HttpResponse response) {
        addOutboundBuffer(response.getBytes());
        enableInterestOp(InterestOp.OP_WRITE);
        wakeup();
    }
}
