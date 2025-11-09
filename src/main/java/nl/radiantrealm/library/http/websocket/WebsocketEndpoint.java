package nl.radiantrealm.library.http.websocket;

public interface WebsocketEndpoint {

    default void onOpen(WebsocketSession session) {}

    default void onFrame(WebsocketSession session, WebsocketFrame frame) {}

    default int availableCapacity() {
        return 0;
    }
}
