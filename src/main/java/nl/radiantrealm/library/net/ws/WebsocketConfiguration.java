package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.io.SocketConfiguration;

public record WebsocketConfiguration(
        int threadPoolSize,
        int incomingBufferSize,
        int outgoingBufferSize,
        int checkTimeoutMillis,
        int sessionTimeoutMillis,
        int pingTimeoutMillis,
        int closeTimeoutMillis
) implements SocketConfiguration {
    public WebsocketConfiguration(
            int threadPoolSize,
            int incomingBufferSize,
            int outgoingBufferSize,
            int checkTimeoutMillis,
            int sessionTimeoutMillis,
            int pingTimeoutMillis,
            int closeTimeoutMillis
    ) {
        this.threadPoolSize = Math.max(1, threadPoolSize);
        this.incomingBufferSize = Math.max(1, incomingBufferSize);
        this.outgoingBufferSize = Math.max(1, outgoingBufferSize);
        this.checkTimeoutMillis = Math.max(1, checkTimeoutMillis);
        this.sessionTimeoutMillis = Math.max(1, sessionTimeoutMillis);
        this.pingTimeoutMillis = Math.max(1, pingTimeoutMillis);
        this.closeTimeoutMillis = Math.max(1, closeTimeoutMillis);
    }

    public static final WebsocketConfiguration defaultConfiguration = new WebsocketConfiguration(
            16,
            1024,
            1024,
            5000,
            300000,
            5000,
            5000
    );
}
