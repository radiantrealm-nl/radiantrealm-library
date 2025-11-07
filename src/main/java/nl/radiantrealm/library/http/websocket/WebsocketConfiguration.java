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
        int maxFrameLength,
        int maxPayloadLength,

        // 3. Protocol and security
        boolean enforceBitMasking,
        boolean allowFragmentation,
        int maxPingIntervalSeconds,
        int sessionTimeoutSeconds
) {
    public static final WebsocketConfiguration defaultConfiguration = new WebsocketConfiguration(
            new InetSocketAddress(0),
            10,
            1024,
            1024,
            1024,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            true,
            true,
            30,
            300
    );
}
