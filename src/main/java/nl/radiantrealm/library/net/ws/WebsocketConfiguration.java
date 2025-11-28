package nl.radiantrealm.library.net.ws;

public record WebsocketConfiguration(
        int threadPoolSize,
        int maxConcurrentlimit,
        int sessionTimeoutMillis,
        int pingTimeoutMillis,
        int closeTimeoutMillis
) {
    public static final WebsocketConfiguration defaultConfiguration = new WebsocketConfiguration(
            20,
            1000,
            300000,
            5000,
            5000
    );
}
