package nl.radiantrealm.library.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.UUID;

public class JsonUtils {
    private static final Gson gson = new Gson();

    private JsonUtils() {}

    public static JsonObject getJsonObject(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for JSON object cannot be null or empty.");

        return gson.fromJson(string, JsonObject.class);
    }

    public static JsonArray getJsonArray(JsonObject object, String key) throws IllegalArgumentException {
        JsonElement element = getJsonElement(object, key);

        if (element == null) {
            throw new IllegalArgumentException(String.format("Key '%s' not found in JSON object.", key));
        }

        if (!element.isJsonArray()) {
            throw new IllegalArgumentException(String.format("Element at key '%s' is not a JSON array.", key));
        }

        return element.getAsJsonArray();
    }

    public static JsonObject getJsonObject(JsonObject object, String key) throws IllegalArgumentException {
        JsonElement element = getJsonElement(object, key);

        if (element == null) {
            throw new IllegalArgumentException(String.format("Key '%s' not found in JSON object.", key));
        }

        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(String.format("Element at key '%s' is not a JSON object.", key));
        }

        return element.getAsJsonObject();
    }

    public static JsonElement getJsonElement(JsonObject object, String key) throws IllegalArgumentException {
        if (object == null) throw new IllegalArgumentException("JSON object cannot be null.");
        if (key == null) throw new IllegalArgumentException("Key for JSON element cannot be null or empty.");

        return object.get(key);
    }

    public static BigDecimal getJsonBigDecimal(JsonObject object, String key) throws IllegalArgumentException {
        return FormatUtils.formatBigDecimal(getJsonString(object, key));
    }

    public static <T extends Enum<T>> T getJsonEnum(Class<T> enumerator, JsonObject object, String key) throws IllegalArgumentException {
        return FormatUtils.formatEnum(enumerator, getJsonString(object, key));
    }

    public static Integer getJsonInteger(JsonObject object, String key) throws IllegalArgumentException {
        return FormatUtils.formatInteger(getJsonString(object, key));
    }

    public static Long getJsonLong(JsonObject object, String key) throws IllegalArgumentException {
        return FormatUtils.formatLong(getJsonString(object, key));
    }

    public static String getJsonString(JsonObject object, String key) throws IllegalArgumentException {
        JsonElement element = getJsonElement(object, key);

        if (element == null) {
            throw new IllegalArgumentException(String.format("Key '%s' not found in JSON object.", key));
        }

        return element.getAsString();
    }

    public static UUID getJsonUUID(JsonObject object, String key) throws IllegalArgumentException {
        return FormatUtils.formatUUID(getJsonString(object, key));
    }
}
