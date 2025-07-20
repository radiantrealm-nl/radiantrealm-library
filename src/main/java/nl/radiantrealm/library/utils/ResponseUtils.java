package nl.radiantrealm.library.utils;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;

public class ResponseUtils {

    private ResponseUtils() {}

    public static void sendResponse(HttpExchange exchange, int statusCode, String mimeType, String body) throws Exception {
        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(statusCode, body.getBytes().length);

            OutputStream stream = exchange.getResponseBody();
            stream.write(body.getBytes());
            stream.close();
        }
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, JsonObject object) throws Exception {
        sendResponse(exchange, statusCode, "application/json", object.toString());
    }

    public static void sendServerError(HttpExchange exchange, String message) throws Exception {
        JsonObject object = new JsonObject();
        object.addProperty("error", message);
        sendResponse(exchange, 500, object);
    }
}
