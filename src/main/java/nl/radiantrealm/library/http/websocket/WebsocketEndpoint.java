package nl.radiantrealm.library.http.websocket;

import java.io.IOException;

public interface WebsocketEndpoint {

    default void onOpen(WebsocketSession session) throws IOException {}

    default void onClose(WebsocketSession session) throws IOException {}

    default void onFrame(WebsocketSession session, WebsocketFrame frame) throws IOException {}
}
