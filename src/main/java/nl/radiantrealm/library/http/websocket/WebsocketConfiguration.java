package nl.radiantrealm.library.http.websocket;

import java.net.InetSocketAddress;

public record WebsocketConfiguration(
        InetSocketAddress address,
        int incomingBufferSize,
        int sessionBufferSize,
        int maxPayloadLength
) {
}
