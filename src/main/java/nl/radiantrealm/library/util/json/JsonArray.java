package nl.radiantrealm.library.util.json;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

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

    @Override
    public @NotNull Iterator<JsonElement> iterator() {
        return list.iterator();
    }

    public static <E> JsonArray fromList(Collection<E> collection, Function<E, JsonElement> function) {
        JsonArray array = new JsonArray(collection.size());

        for (E e : collection) {
            array.add(function.apply(e));
        }

        return array;
    }

    public List<JsonElement> asList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public boolean contains(JsonElement element) {
        return list.contains(element);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public JsonElement get(int index) {
        return list.get(index);
    }

    public JsonElement getFirst() {
        return list.getFirst();
    }

    public JsonElement getLast() {
        return list.getLast();
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

    public void clear() {
        list.clear();
    }
}
