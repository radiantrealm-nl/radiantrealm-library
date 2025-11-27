package nl.radiantrealm.library.net.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public abstract class Server extends SelectorEngine {
    protected final ServerSocketChannel serverChannel;

    public Server(InetSocketAddress socketAddress) throws IOException {
        super(20);

        this.serverChannel = createServerChannel(socketAddress);
    }

    protected ServerSocketChannel createServerChannel(InetSocketAddress socketAddress) throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.bind(socketAddress);
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        return socketChannel;
    }
}
