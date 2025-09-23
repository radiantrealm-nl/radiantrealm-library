package nl.radiantrealm.library.utils.json;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public record JsonArrayWrapper(JsonArray array) implements Iterable<JsonElement> {
    public static final Gson GSON = JsonUtils.GSON;

    public JsonArrayWrapper(String string) {
        this(
                GSON.fromJson(string, JsonArray.class)
        );
    }

    @NotNull
    @Override
    public Iterator<JsonElement> iterator() {
        return array.iterator();
    }

    @CanIgnoreReturnValue
    public JsonElement set(int index, JsonElement element) {
        return array.set(index, element);
    }

    @CanIgnoreReturnValue
    public boolean remove(JsonElement element) {
        return array.remove(element);
    }

    public JsonElement remove(int index) {
        return array.remove(index);
    }

    public void add(JsonElement element) {
        array.add(element);
    }

    public void add(Boolean bool) {
        array.add(bool);
    }

    public void add(Character character) {
        array.add(character);
    }

    public void add(Number number) {
        array.add(number);
    }

    public void add(String string) {
        array.add(string);
    }

    public JsonElement getJsonElement(int index) {
        return array.get(index);
    }

    @Override
    public String toString() {
        return array.getAsString();
    }

    public <T> List<T> getList(JsonArray array, Function<JsonElement, T> function) {
        return JsonUtils.getList(array, function);
    }
}
