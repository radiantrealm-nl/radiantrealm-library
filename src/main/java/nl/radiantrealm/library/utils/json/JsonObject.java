package nl.radiantrealm.library.utils.json;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class JsonObject extends JsonElement {
    private final LinkedHashMap<String, JsonElement> map;

    public JsonObject() {
        this.map = new LinkedHashMap<>();
    }

    public JsonObject(int capacity) {
        this.map = new LinkedHashMap<>(capacity);
    }

    @Override
    public JsonElement deepCopy() {
        if (map.isEmpty()) {
            return new JsonObject();
        }

        JsonObject object = new JsonObject(map.size());

        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
            object.add(entry.getKey(), entry.getValue());
        }

        return object;
    }

    public static <K, V> JsonObject fromMap(Map<K, V> map, Function<K, String> keyFunction, Function<V, JsonElement> valueFunction) {
        JsonObject object = new JsonObject(map.size());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            object.add(
                    keyFunction.apply(entry.getKey()),
                    valueFunction.apply(entry.getValue())
            );
        }

        return object;
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return map.entrySet();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<JsonElement> values() {
        return map.values();
    }

    public LinkedHashMap<String, JsonElement> asMap() {
        return map;
    }

    public JsonElement get(String key) {
        return map.get(key);
    }

    public JsonArray getAsJsonArray(String key) {
        return map.get(key).getAsJsonArray();
    }

    public JsonNull getAsJsonNull(String key) {
        return map.get(key).getAsJsonNull();
    }

    public JsonObject getAsJsonObject(String key) {
        return map.get(key).getAsJsonObject();
    }

    public JsonPrimitive getAsJsonPrimitive(String key) {
        return map.get(key).getAsJsonPrimitive();
    }

    public boolean getAsBoolean(String key) {
        return map.get(key).getAsBoolean();
    }

    public Number getAsNumber(String key) {
        return map.get(key).getAsNumber();
    }

    public String getAsString(String key) {
        return map.get(key).getAsString();
    }

    public UUID getAsUUID(String key) {
        return map.get(key).getAsUUID();
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(String key, Class<T> enumerator) {
        return map.get(key).getAsEnum(enumerator);
    }

    public BigDecimal getAsBigDecimal(String key) {
        return map.get(key).getAsBigDecimal();
    }

    public void add(String key, JsonElement element) {
        map.put(key, element == null ? JsonNull.INSTANCE : element);
    }

    public void add(String key, Boolean bool) {
        map.put(key, bool == null ? JsonNull.INSTANCE : new JsonPrimitive(bool));
    }

    public void add(String key, Number number) {
        map.put(key, number == null ? JsonNull.INSTANCE : new JsonPrimitive(number));
    }

    public void add(String key, String string) {
        map.put(key, string == null ? JsonNull.INSTANCE : new JsonPrimitive(string));
    }

    public void add(String key, UUID uuid) {
        add(key, uuid.toString());
    }

    public void add(String key, Enum<?> enumerator) {
        add(key, enumerator.name());
    }

    public void addAll(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void remove(String key) {
        map.remove(key);
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }
}
