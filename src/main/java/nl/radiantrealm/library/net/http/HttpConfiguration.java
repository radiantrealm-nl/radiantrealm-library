package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.SocketConfiguration;

public record HttpConfiguration(
        int threadPoolSize,
        int incomingBufferSize,
        int outgoingBufferSize
) implements SocketConfiguration {
    public static final HttpConfiguration defaultConfiguration = new HttpConfiguration(
            16,
            4096,
            4096
    );
}
