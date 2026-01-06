package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpConnection;
import nl.radiantrealm.library.net.http.HttpHandler;
import nl.radiantrealm.library.net.http.HttpRequest;
import nl.radiantrealm.library.net.http.HttpResponse;

import java.io.IOException;

public abstract class WebsocketServer extends WebsocketEngine implements HttpHandler {

    public WebsocketServer(WebsocketConfiguration configuration) throws IOException {
        super(configuration);
    }

    @Override
    public void handle(HttpConnection connection, HttpRequest request) throws Exception {
        HttpResponse response = WebsocketHandshake.performHandshake(request);

        connection.key.cancel();
        connection.addOutboundBuffer(response.getBytes());
        pendingUpgrades.add(connection);
        selector.wakeup();
    }
}
