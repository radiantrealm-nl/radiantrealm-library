package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Logger;
import nl.radiantrealm.library.utils.ResponseUtils;

public abstract class RequestHandler {
    private static final Logger logger = Logger.getLogger(RequestHandler.class);

    public RequestHandler() {}

    protected record Request(String method, JsonObject object) {}

    protected record Response(int statusCode, JsonObject object) {}

    public void handle(HttpExchange exchange) {
        try {
            Response response = handle(new Request(
                    exchange.getRequestMethod(),
                    JsonUtils.getJsonObject(exchange)
            ));

            ResponseUtils.sendResponse(exchange, response.statusCode, response.object);
        } catch (Exception e) {
            logger.error("Failed to send response.", e);
        }
    }

    protected abstract Response handle(Request request) throws Exception;
}
