package nl.radiantrealm.library.net.io;

public interface SocketConfiguration extends SelectorConfiguration {
    int incomingBufferSize();
    int outgoingBufferSize();
}
