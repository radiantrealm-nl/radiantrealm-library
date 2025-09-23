package nl.radiantrealm.library.utils.json;

import com.google.gson.*;
import nl.radiantrealm.library.utils.format.FormatUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class JsonUtils {
    public static final Gson GSON = new Gson();

    public static BigDecimal getBigDecimal(JsonObject object, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return primitive.getAsBigDecimal();
    }

    public static Boolean getBoolean(JsonObject object, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }

        return FormatUtils.formatBoolean(primitive.getAsString());
    }

    public static <T extends Enum<T>> T getEnum(JsonObject object, Class<T> enumerator, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return FormatUtils.formatEnum(enumerator, primitive.getAsString());
    }

    public static Integer getInteger(JsonObject object, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return primitive.getAsInt();
    }

    public static <T> List<T> getList(JsonArray array, Function<JsonElement, T> function) {
        List<T> list = new ArrayList<>(array.size());

        for (JsonElement element : array) {
            list.add(function.apply(element));
        }

        return list;
    }

    public static Long getLong(JsonObject object, String key) {
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

    public static String getString(JsonObject object, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return primitive.getAsString();
    }

    public static UUID getUUID(JsonObject object, String key) {
        JsonPrimitive primitive = object.getAsJsonPrimitive(key);

        if (primitive == null) {
            return null;
        }

        return UUID.fromString(primitive.getAsString());
    }
}
