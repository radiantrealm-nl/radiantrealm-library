package nl.radiantrealm.library.utils;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;

public class ResponseUtils {

    private ResponseUtils() {}

    public static void sendResponse(HttpExchange exchange, int statusCode, JsonObject object) throws Exception {
        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, object.toString().getBytes().length);

            OutputStream stream = exchange.getResponseBody();
            stream.write(object.toString().getBytes());
            stream.close();
        }
    }
}
