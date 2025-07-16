package nl.radiantrealm.library.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonUtils {
    private static final Gson gson = new Gson();

    private JsonUtils() {
        Parsable<JsonElement> parsable = getJsonElement(new JsonObject(), "");
    }

    public static Parsable<JsonObject> getJsonObject(String string) {
        try {
            if (string == null) {
                throw new IllegalArgumentException("Unable to parse Json from an empty body.");
            }

            return new Parsable<>(
                    Optional.of(gson.fromJson(string, JsonObject.class)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Parsable<JsonObject> getJsonObject(HttpExchange exchange) {
        try (InputStream stream = exchange.getRequestBody()) {
            String body = new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            return getJsonObject(body);
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Parsable<JsonElement> getJsonElement(JsonObject object, String key) {
        try {
            if (object == null) {
                throw new IllegalArgumentException("Unable to parse from an empty body.");
            }

            JsonElement element = object.get(key);

            if (element == null) {
                throw new JsonSyntaxException("Could not find key.");
            }

            return new Parsable<>(
                    Optional.of(element),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }
}
