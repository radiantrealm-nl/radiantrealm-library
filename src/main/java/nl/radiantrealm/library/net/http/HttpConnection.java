package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.SelectorEngine;
import nl.radiantrealm.library.net.io.SocketConnection;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class HttpConnection extends SocketConnection {
    public final AggregatedHttpRequest aggregatedHttpRequest = new AggregatedHttpRequest(this);

    public HttpConnection(SelectorEngine engine, SelectionKey key, SocketChannel channel) {
        super(engine, key, channel);
    }
}
