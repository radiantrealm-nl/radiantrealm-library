package nl.radiantrealm.library.http.websocket;

import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;

public record WebsocketSession(InputStream inputStream, OutputStream outputStream) {

    public WebsocketSession(HttpExchange exchange) {
        this(exchange.getRequestBody(), exchange.getResponseBody());
    }

    public byte[] readBytes(int length) throws Exception {
        return inputStream.readNBytes(length);
    }

    public void sendBytes(byte... bytes) throws Exception {
        outputStream.write(bytes);
    }

    public void flush() throws Exception {
        outputStream.flush();
    }

    public boolean available() throws Exception {
        return inputStream.available() > 0;
    }
}
