package nl.radiantrealm.library.utils.json;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class JsonElement {

    public abstract JsonElement deepCopy();

    @Override
    public String toString() {
        return toString(new StringBuilder(), false);
    }

    public String toString(boolean prettyPrinting) {
        return toString(new StringBuilder(), prettyPrinting);
    }

    protected String toString(StringBuilder builder, boolean prettyPrint) {
        return switch (this) {
            case JsonArray array -> {
                builder.append(prettyPrint ? "[\n" : '[');
                Set<String> strings = new HashSet<>(array.size());

                for (JsonElement element : array) {
                    strings.add(String.format(element.toString(new StringBuilder(), prettyPrint)));
                }

                builder.append(String.join(prettyPrint ? ",\n" : ",", strings));
                yield builder.append(prettyPrint ? "\n]" : ']').toString();
            }

            case JsonObject object -> {
                builder.append(prettyPrint ? "{\n" : '{');
                Set<String> strings = new HashSet<>(object.size());

                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    strings.add(String.format(
                            "\"%s\"%s%s",
                            entry.getKey(),
                            prettyPrint ? ": " : ':',
                            entry.getValue().toString(new StringBuilder(), prettyPrint))
                    );
                }

                builder.append(String.join(prettyPrint ? ",\n" : ",", strings));
                yield builder.append('}').toString();
            }

            case JsonPrimitive primitive -> switch (primitive.object) {
                case Boolean bool -> builder.append(bool).toString();
                case Number number -> builder.append(number).toString();
                case String string -> builder.append(String.format("\"%s\"", string)).toString();
                default -> throw new IllegalArgumentException("Unknown Json primitive type.");
            };

            case JsonNull jsonNull -> builder.append(jsonNull).toString();
            default -> throw new IllegalStateException();
        };
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
