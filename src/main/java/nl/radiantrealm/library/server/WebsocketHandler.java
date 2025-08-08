package nl.radiantrealm.library.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class WebsocketHandler {

    public abstract void handle(HttpExchange exchange, InputStream inputStream, OutputStream outputStream);
}
