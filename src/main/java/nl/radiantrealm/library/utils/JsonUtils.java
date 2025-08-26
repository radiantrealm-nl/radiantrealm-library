package nl.radiantrealm.library.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonUtils {
    private static final Gson gson = new Gson();

    private JsonUtils() {}

    /**
     * Formats a {@link String} into a {@link JsonObject}, utilizing the {@link Result} wrapper.
     * The converting is done through the {@link Gson} utility tool.
     *
     * @param string Input value to format into a {@link JsonObject}.
     * @return A {@link Result} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Result<JsonObject> getJsonObject(String string) {
        try {
            if (string == null) {
                throw new IllegalArgumentException("Unable to parse Json from an empty body.");
            }

            return new Result<>(
                    Optional.of(gson.fromJson(string, JsonObject.class)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    /**
     * Retrieves a {@link JsonElement} from a {@link JsonObject}, utilizing the {@link Result} wrapper.
     *
     * @param object The {@link JsonObject} containing the element.
     * @param key Key to retrieve {@link JsonElement} from.
     * @return A {@link Result} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Result<JsonElement> getJsonElement(JsonObject object, String key) {
        try {
            if (object == null) {
                throw new IllegalArgumentException("Unable to parse from an empty body.");
            }

            JsonElement element = object.get(key);

            if (element == null) {
                throw new JsonSyntaxException("Could not find key.");
            }

            return new Result<>(
                    Optional.of(element),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Result<BigDecimal> getJsonBigDecimal(JsonObject object, String key) {
        try {
            Result<JsonElement> element = getJsonElement(object, key);

            if (element.isObjectEmpty()) {
                return new Result<>(
                        Optional.empty(),
                        Optional.of(element.getError())
                );
            }

            return FormatUtils.parseBigDecimal(element.getObject().getAsString());
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Result<Boolean> getJsonBoolean(JsonObject object, String key) {
        try {
            Result<JsonElement> element = getJsonElement(object, key);

            if (element.isObjectEmpty()) {
                return new Result<>(
                        Optional.empty(),
                        Optional.of(element.getError())
                );
            }

            return FormatUtils.parseBoolean(element.getObject().getAsString());
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Result<Integer> getJsonInteger(JsonObject object, String key) {
        try {
            Result<JsonElement> element = getJsonElement(object, key);

            if (element.isObjectEmpty()) {
                return new Result<>(
                        Optional.empty(),
                        Optional.of(element.getError())
                );
            }

            return FormatUtils.parseInteger(element.getObject().getAsString());
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Result<String> getJsonString(JsonObject object, String key) {
        try {
            Result<JsonElement> element = getJsonElement(object, key);

            if (element.isObjectEmpty()) {
                return new Result<>(
                        Optional.empty(),
                        Optional.of(element.getError())
                );
            }

            return new Result<>(
                    Optional.of(element.getObject().getAsString()),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    /**
     * Retrieves a {@link UUID} from a {@link JsonObject}, utilizing the {@link Result} wrapper.
     *
     * @param object The {@link JsonObject} containinig the element.
     * @param key Key to retrieve {@link JsonElement}, converted to a {@link UUID}.
     * @return A {@link Result} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Result<UUID> getJsonUUID(JsonObject object, String key) {
        try {
            Result<JsonElement> element = getJsonElement(object, key);

            if (element.isObjectEmpty()) {
                return new Result<>(
                        Optional.empty(),
                        Optional.of(element.getError())
                );
            }

            return FormatUtils.parseUUID(element.getObject().getAsString());
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }
}
