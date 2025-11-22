package nl.radiantrealm.library.util.json;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class JsonElement {

    public abstract JsonElement deepCopy();

    @Override
    public String toString() {
        return JsonPrinter.print(this);
    }

    public String toString(boolean prettyPrinting) {
        return JsonPrinter.print(this, prettyPrinting);
    }

    public String toString(JsonPrintingContext context) {
        return JsonPrinter.print(this, context);
    }

    public JsonArray getAsJsonArray() {
        if (this instanceof JsonArray jsonArray) {
            return jsonArray;
        }

        throw new IllegalStateException("Not an instance of JSON Array.");
    }

    public boolean isJsonArray() {
        return this instanceof JsonArray;
    }

    public JsonNull getAsJsonNull() {
        if (this instanceof JsonNull jsonNull) {
            return jsonNull;
        }

        throw new IllegalStateException("Not an instance of JSON Null.");
    }

    public boolean isJsonNull() {
        return this instanceof JsonNull;
    }

    public JsonObject getAsJsonObject() {
        if (this instanceof JsonObject jsonObject) {
            return jsonObject;
        }

        throw new IllegalStateException("Not an instance of JSON Object.");
    }

    public boolean isJsonObject() {
        return this instanceof JsonObject;
    }

    public JsonPrimitive getAsJsonPrimitive() {
        if (this instanceof JsonPrimitive jsonPrimitive) {
            return jsonPrimitive;
        }

        throw new IllegalStateException("Not an instance of JSON Primitive.");
    }

    public boolean isJsonPrimitive() {
        return this instanceof JsonPrimitive;
    }

    public boolean getAsBoolean() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public Number getAsNumber() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public String getAsString() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public UUID getAsUUID() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(Class<T> enumerator) {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public BigDecimal getAsBigDecimal() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }
}
