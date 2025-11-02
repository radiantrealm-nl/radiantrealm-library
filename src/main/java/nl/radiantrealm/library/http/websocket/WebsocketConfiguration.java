package nl.radiantrealm.library.http.websocket;

public record WebsocketConfiguration(
        boolean allowFragmentation,
        boolean enforceBitMasking,
        long maxPayloadLength
) {
    public static final WebsocketConfiguration defaultConfiguration = new WebsocketConfiguration(
            false,
            false,
            16777216
    );
}
