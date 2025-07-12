package nl.radiantrealm.library.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.utils.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class RequestHandler {
    private static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(RequestHandler.class);

    public RequestHandler() {}

    public void handle(HttpExchange exchange) {
        try (InputStream stream = exchange.getRequestBody()) {
            Optional<JsonObject> object = parseRequestBody(stream);

            Response response = handle(new Request(
                    exchange.getRequestMethod(),
                    object
            ));

            sendResponse(exchange, response);
        } catch (JsonSyntaxException e) {
            JsonObject object = new JsonObject();
            object.addProperty("error", "Invalid Json body.");
            sendResponse(exchange, new Response(400, Optional.of(object)));
        } catch (Exception e) {
            logger.debug("Unexpected error.", e);
            sendResponse(exchange, new Response(500, Optional.empty()));
        }
    }

    protected abstract Response handle(Request request) throws Exception;

    private Optional<JsonObject> parseRequestBody(InputStream stream) {
        String body = new BufferedReader(new InputStreamReader(stream))
                .lines()
                .collect(Collectors.joining("\n"));

        return Optional.of(gson.fromJson(body, JsonObject.class));
    }

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
