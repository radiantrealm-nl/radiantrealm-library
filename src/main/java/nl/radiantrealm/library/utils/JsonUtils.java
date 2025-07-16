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
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonUtils {
    private static final Gson gson = new Gson();

    private JsonUtils() {}

    /**
     * Formats a {@link String} into a {@link JsonObject}, utilizing the {@link Parsable} wrapper.
     * The converting is done through the {@link Gson} utility tool.
     *
     * @param string Input value to format into a {@link JsonObject}.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
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

    /**
     * Formats the request body of a {@link HttpExchange} into a {@link JsonObject}, utiliziing the {@link Parsable} wrapper.
     * This methods reads the input stream from the HTTP request body, converts it to a string and attempts to parse it to a Json object.
     *
     * @param exchange The incoming {@link HttpExchange} containing the HTTP request body to parse.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
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

    /**
     * Retrieves a {@link JsonElement} from a {@link JsonObject}, utilizing the {@link Parsable} wrapper.
     *
     * @param object The {@link JsonObject} containing the element.
     * @param key Key to retrieve {@link JsonElement} from.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
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

    /**
     * Retrieves a {@link UUID} from a {@link JsonObject}, utilizing the {@link Parsable} wrapper.
     *
     * @param object The {@link JsonObject} containinig the element.
     * @param key Key to retrieve {@link JsonElement}, converted to a {@link UUID}.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Parsable<UUID> getJsonUUID(JsonObject object, String key) {
        try {
            Parsable<JsonElement> element = getJsonElement(object, key);

            if (element.object().isEmpty()) {
                return new Parsable<>(
                        Optional.empty(),
                        Optional.of(element.getThrowable())
                );
            }

            return FormatUtiis.parseUUID(element.getObject().getAsString());
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }
}
