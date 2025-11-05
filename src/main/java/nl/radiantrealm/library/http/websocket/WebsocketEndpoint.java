package nl.radiantrealm.library.http.websocket;

public abstract class WebsocketEndpoint {

    public abstract void onFrame(WebsocketSession session, WebsocketFrame frame);

    public abstract void sendFrame(WebsocketSession session, WebsocketFrame frame);
}
