package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Logger;
import nl.radiantrealm.library.utils.Result;

import java.io.OutputStream;
import java.util.Optional;

public abstract class RequestHandler {
    private static final Logger logger = Logger.getLogger(RequestHandler.class);

    private final boolean debug;

    public RequestHandler(boolean debug) {
        this.debug = debug;
    }

    public void handle(HttpExchange exchange) {
        Result<JsonObject> parsable = JsonUtils.getJsonObject(exchange);

        if (parsable.object().isEmpty()) {
            JsonObject object = new JsonObject();
            object.addProperty("error", parsable.getThrowable().getMessage());
            sendResponse(exchange, new Response(400, Optional.of(object)));
            return;
        }

        try {
            Response response = handle(new Request(
                    exchange.getRequestMethod(),
                    parsable.object()
            ));

            sendResponse(exchange, response);
        } catch (Exception e) {
            if (debug) {
                logger.debug("Unexpected error.", e);
            }

            sendResponse(exchange, new Response(500, Optional.empty()));
        }
    }

    protected abstract Response handle(Request request) throws Exception;

    private void sendResponse(HttpExchange exchange, Response response) {
        JsonObject object = response.object().orElse(new JsonObject());

        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.statusCode(), response.toString().getBytes().length);

            OutputStream stream = exchange.getResponseBody();
            stream.write(object.toString().getBytes());
            stream.close();
        } catch (Exception e) {
            logger.error("Failed to send response.", e);
        }
    }
}
