package nl.radiantrealm.library.utils.json;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.*;
import nl.radiantrealm.library.http.model.HttpRequest;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public record JsonObjectWrapper(JsonObject object) {
    public static final Gson GSON = JsonUtils.GSON;

    public JsonObjectWrapper(String string) {
        this(
                GSON.fromJson(string, JsonObject.class)
        );
    }

    public JsonObjectWrapper(HttpRequest request) {
        this(
                request.getRequestBody()
        );
    }

    @Override
    public String toString() {
        return object.getAsString();
    }

    public JsonObject deepcopy() {
        return object.deepCopy();
    }

    public void add(String keyy, JsonElement element) {
        object.add(keyy, element);
    }

    public void addAll(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            object.add(entry.getKey(), entry.getValue());
        }
    }

    @CanIgnoreReturnValue
    public JsonElement remove(String key) {
        return object.remove(key);
    }

    public void addProperty(String key, String string) {
        object.addProperty(key, string);
    }

    public void addProperty(String key, Number number) {
        object.addProperty(key, number);
    }

    public void addProperty(String key, Boolean bool) {
        object.addProperty(key, bool);
    }

    public void addProperty(String key, Character character) {
        object.addProperty(key, character);
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return object.entrySet();
    }

    public Set<String> keySet() {
        return object.keySet();
    }

    public Collection<JsonElement> values() {
        return object.asMap().values();
    }

    public int size() {
        return object.size();
    }

    public boolean isEmpty() {
        return object.isEmpty();
    }

    public boolean has(String key) {
        return object.has(key);
    }

    public JsonElement getJsonElement(String key) {
        return object.get(key);
    }

    public JsonPrimitive getJsonPrimitive(String key) {
        return object.getAsJsonPrimitive(key);
    }

    public JsonArray getJsonArray(String key) {
        return object.getAsJsonArray(key);
    }

    public JsonObject getJsonObject(String key) {
        return object.getAsJsonObject(key);
    }

    public Map<String, JsonElement> asMap() {
        return object.asMap();
    }

    public <K, V> Map<K, V> getMap(Function<String, K> key, Function<JsonElement, V> value) {
        return JsonUtils.getMap(object, key, value);
    }

    public BigDecimal getBigDecimal(String key) {
        return JsonUtils.getBigDecimal(object, key);
    }

    public Boolean getBoolean(String key) {
        return JsonUtils.getBoolean(object, key);
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumerator, String key) {
        return JsonUtils.getEnum(object, enumerator, key);
    }

    public Integer getInteger(String key) {
        return JsonUtils.getInteger(object, key);
    }

    public Long getLong(String key) {
        return JsonUtils.getLong(object, key);
    }

    public String getString(String key) {
        return JsonUtils.getString(object, key);
    }

    public UUID getUUID(String key) {
        return JsonUtils.getUUID(object, key);
    }
}
