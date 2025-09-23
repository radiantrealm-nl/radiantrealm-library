package nl.radiantrealm.library.utils.json;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    public JsonArrayWrapper(JsonObject object, String key) {
        this(
                object.getAsJsonArray(key)
        );
    }

    public JsonArray deepCopy() {
        return array.deepCopy();
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

    public void add(JsonElement element) {
        array.add(element);
    }

    public void addAll(JsonArray array) {
        array.addAll(array);
    }

    @CanIgnoreReturnValue
    public JsonElement set(int index, JsonElement element) {
        return array.set(index, element);
    }

    @CanIgnoreReturnValue
    public boolean remove(JsonElement element) {
        return array.remove(element);
    }

    @CanIgnoreReturnValue
    public JsonElement remove(int index) {
        return array.remove(index);
    }

    public boolean contains(JsonElement element) {
        return array.contains(element);
    }

    public boolean isEmpty() {
        return array.isEmpty();
    }

    @Override @NotNull
    public Iterator<JsonElement> iterator() {
        return array.iterator();
    }

    public JsonElement get(int index) {
        return array.get(index);
    }

    public Number getAsNumber() {
        return array.getAsNumber();
    }

    public String getAsString() {
        return array.getAsString();
    }

    public double getAsDouble() {
        return array.getAsDouble();
    }

    public BigDecimal getAsBigDecimal() {
        return array.getAsBigDecimal();
    }

    public BigInteger getAsBigInteger() {
        return array.getAsBigInteger();
    }

    public float getAsFloat() {
        return array.getAsFloat();
    }

    public long getAsLong() {
        return array.getAsLong();
    }

    public int getAsInt() {
        return array.getAsInt();
    }

    public byte getAsByte() {
        return array.getAsByte();
    }

    public short getAsShort() {
        return array.getAsShort();
    }

    public boolean getAsBoolean() {
        return array.getAsBoolean();
    }

    public List<JsonElement> asList() {
        return array.asList();
    }

    public <T> List<T> getList(Function<JsonElement, T> function) {
        return JsonUtils.getList(array, function);
    }
}
