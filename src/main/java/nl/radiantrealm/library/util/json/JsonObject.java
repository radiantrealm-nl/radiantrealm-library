package nl.radiantrealm.library.util.json;

import java.util.*;

public class JsonObject extends HashMap<String, JsonElement> implements JsonContainer {

    public JsonObject() {}

    public JsonObject(int initialCapacity) {
        super(initialCapacity);
    }

    public JsonObject(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public JsonObject(Map<? extends String, ? extends JsonElement> map) {
        super(map);
    }

    public JsonObject(String input) {
        putAll(JsonReader.parse(input).getAsJsonObject());
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }

        String[] strings = new String[size()];

        int index = 0;
        for (Map.Entry<String, JsonElement> entry : entrySet()) {
            if (entry.getKey() != null) {
                JsonElement element = entry.getValue();

                if (element == null) {
                    element = JsonNull.INSTANCE;
                }

                strings[index++] = String.format(
                        "\"%s\":%s",
                        entry.getKey(),
                        element
                );
            }
        }

        return String.format(
                "{%s}",
                String.join(",", strings)
        );
    }

    public JsonElement put(String name, boolean value) {
        return put(name, JsonBoolean.valueOf(value));
    }

    public JsonElement put(String name, String value) {
        return put(name, new JsonString(value));
    }

    public JsonElement put(String name, Number value) {
        return put(name, new JsonNumber(value));
    }

    @Override
    public String prettyPrint(int depth, int offset) {
        if (isEmpty()) {
            return "{}";
        }

        depth = Math.max(0, depth);
        offset = Math.max(0, offset);
        String[] strings = new String[size()];
        String depthOffset = " ".repeat(depth + offset);

        int index = 0;
        for (Map.Entry<String, JsonElement> entry : entrySet()) {
            if (entry.getKey() != null) {
                JsonElement element = entry.getValue() == null ? JsonNull.INSTANCE : entry.getValue();

                strings[index++] = String.format(
                        "%s\"%s\": %s",
                        depthOffset,
                        entry.getKey(),
                        element.prettyPrint(depth, offset + depth)
                );
            }
        }

        return String.format(
                "{\n%s\n%s}",
                String.join(",\n", strings),
                " ".repeat(offset)
        );
    }
}
