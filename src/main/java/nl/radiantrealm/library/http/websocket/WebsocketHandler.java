package nl.radiantrealm.library.http.websocket;

import java.io.InputStream;
import java.io.OutputStream;

public interface WebsocketHandler {
    void handle(InputStream inputStream, OutputStream outputStream) throws Exception;
}
