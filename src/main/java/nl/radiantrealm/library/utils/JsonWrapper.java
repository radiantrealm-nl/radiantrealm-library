package nl.radiantrealm.library.utils;

import com.google.gson.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record JsonWrapper(JsonObject object) {
    public static final Gson GSON = new Gson();

    public JsonWrapper(String string) {
        this(
                GSON.fromJson(string, JsonObject.class)
        );
    }

    public JsonWrapper(JsonObject object, String key) {
        this(
                object.getAsJsonObject(key)
        );
    }

    @Override
    public String toString() {
        return object.getAsString();
    }

    public JsonObject getJsonObject(String key) {
        return object.getAsJsonObject(key);
    }

    public JsonArray getJsonArray(String key) {
        return object.getAsJsonArray(key);
    }

    public JsonElement getJsonElement(String key) {
        return object.get(key);
    }

    public BigDecimal getBigDecimal(String key) {
        JsonElement element = object.get(key);

        if (element == null) {
            return null;
        }

        return element.getAsBigDecimal();
    }

    public Boolean getBoolean(String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }

        return FormatUtils.formatBoolean(primitive.getAsString());
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumerator, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return FormatUtils.formatEnum(enumerator, primitive.getAsString());
    }

    public Integer getInteger(String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return primitive.getAsInt();
    }

    public Long getLong(String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return primitive.getAsLong();
    }

    public static <K, V> Map<K, V> getMap(JsonObject object, Function<String, K> key, Function<JsonElement, V> value) {
        Map<K, V> map = new HashMap<>(object.size());

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(key.apply(entry.getKey()), value.apply(entry.getValue()));
        }

        return map;
    }

    public <K, V> Map<K, V> getMap(Function<String, K> key, Function<JsonElement, V> value) {
        return getMap(object, key, value);
    }

    public String getString(String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return primitive.getAsString();
    }

    public UUID getUUID(String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return UUID.fromString(primitive.getAsString());
    }
}
