package nl.radiantrealm.library.util.json;

import java.util.*;

public class JsonArray extends ArrayList<JsonElement> implements JsonContainer {

    public JsonArray() {}

    public JsonArray(int initialCapacity) {
        super(initialCapacity);
    }

    public JsonArray(Collection<? extends JsonElement> collection) {
        super(collection);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        String[] strings = new String[size()];

        int index = 0;
        for (JsonElement element : this) {
            if (element == null) {
                element = JsonNull.INSTANCE;
            }

            strings[index++] = element.toString();
        }

        return String.format(
                "[%s]",
                String.join(",", strings)
        );
    }

    public boolean add(boolean value) {
        return add(JsonBoolean.valueOf(value));
    }

    public boolean add(String value) {
        return add(new JsonString(value));
    }

    public boolean add(Number value) {
        return add(new JsonNumber(value));
    }

    public void add(int index, boolean value) {
        add(index, JsonBoolean.valueOf(value));
    }

    public void add(int index, String value) {
        add(index, new JsonString(value));
    }

    public void add(int index, Number value) {
        add(index, new JsonNumber(value));
    }

    @Override
    public String prettyPrint(int depth, int offset) {
        if (isEmpty()) {
            return "[]";
        }

        depth = Math.max(0, depth);
        offset = Math.max(0, offset);
        String[] strings = new String[size()];
        String depthOffset = " ".repeat(depth + offset);

        int index = 0;
        for (JsonElement element : this) {
            element = element == null ? JsonNull.INSTANCE : element;

            strings[index++] = String.format(
                    "%s%s",
                    depthOffset,
                    element.prettyPrint(depth, offset + depth)
            );
        }

        return String.format(
                "[\n%s\n%s]",
                String.join(",\n", strings),
                " ".repeat(offset)
        );
    }
}
