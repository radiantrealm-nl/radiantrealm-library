package nl.radiantrealm.library.utils.json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class JsonArray extends JsonElement implements Iterable<JsonElement> {
    private final ArrayList<JsonElement> list;

    public JsonArray() {
        this.list = new ArrayList<>();
    }

    public JsonArray(int capacity) {
        this.list = new ArrayList<>(capacity);
    }

    @Override
    public JsonElement deepCopy() {
        if (list.isEmpty()) {
            return new JsonArray();
        }

        JsonArray array = new JsonArray(list.size());

        for (JsonElement element : list) {
            array.add(element);
        }

        return array;
    }

    public void add(JsonElement element) {
        list.add(element == null ? JsonNull.INSTANCE : element);
    }

    public void add(Boolean bool) {
        list.add(bool == null ? JsonNull.INSTANCE : new JsonPrimitive(bool));
    }

    public void add(Number number) {
        list.add(number == null ? JsonNull.INSTANCE : new JsonPrimitive(number));
    }

    public void add(String string) {
        list.add(string == null ? JsonNull.INSTANCE : new JsonPrimitive(string));
    }

    public void add(UUID uuid) {
        add(uuid.toString());
    }

    public void add(Enum<?> enumerator) {
        add(enumerator.name());
    }

    public void addAll(JsonArray array) {
        list.addAll(array.list);
    }

    public void remove(JsonElement element) {
        list.remove(element);
    }

    public boolean contains(JsonElement element) {
        return list.contains(element);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public @NotNull Iterator<JsonElement> iterator() {
        return list.iterator();
    }

    public List<JsonElement> asList() {
        return list;
    }
}
