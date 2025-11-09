package nl.radiantrealm.library.http.websocket;

import java.net.InetSocketAddress;

public record WebsocketConfiguration(
        // 1. Address
        InetSocketAddress socketAddress,

        // 2. Peformance and sizing
        int workingIOThreads,
        int maxActiveSessions,
        int incomingBufferSize,
        int sessionBufferSize,
        int maxPayloadLength,

        // 3. Protocol and security
        boolean enforceBitMasking,
        boolean allowFragmentation,
        int sessionTimeoutMillis,
        int pongTimeoutMillis,
        int closeTimeoutMillis
) {
    public static WebsocketConfiguration defaultConfiguration(InetSocketAddress socketAddress) {
        return new WebsocketConfiguration(
                socketAddress,
                10,
                1024,
                1024,
                1024,
                16777216,
                true,
                true,
                300000,
                5000,
                5000
        );
    }
}
