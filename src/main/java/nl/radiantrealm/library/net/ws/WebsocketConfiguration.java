package nl.radiantrealm.library.net.ws;

public record WebsocketConfiguration(
        int threadPoolSize,
        int maxActiveSessions,
        int incomingBufferSize,
        int outgoingBufferSize,
        int checkTimeoutMillis,
        int sessionTimeoutMillis,
        int pingTimeoutMillis,
        int closeTimeoutMillis
) {
    public static final WebsocketConfiguration defaultConfiguration = new WebsocketConfiguration(
            16,
            1024,
            1024,
            1024,
            5000,
            300000,
            5000,
            5000
    );
}
