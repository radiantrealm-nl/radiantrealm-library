package nl.radiantrealm.library.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class JsonUtils {
    private static final Gson gson = new Gson();

    private JsonUtils() {}

    /**
     * Formats the request body of a {@link HttpExchange} into a {@link JsonObject}.
     *
     * @param exchange              the http request to retreive the Json from
     * @throws Exception            on failure during InputStream/BufferedReader
     * @return the parsed Json object.
     * */
    public static JsonObject getJsonObject(HttpExchange exchange) throws Exception {
        try (InputStream stream = exchange.getRequestBody()) {
            String body = new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            return getJsonObject(body);
        }
    }

    public static JsonObject getJsonObject(String string) {
        return gson.fromJson(string, JsonObject.class);
    }
}
