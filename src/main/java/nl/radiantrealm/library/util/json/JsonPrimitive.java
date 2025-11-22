package nl.radiantrealm.library.util.json;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class JsonPrimitive extends JsonElement {
    public final Object object;

    public JsonPrimitive(Boolean bool) {
        this.object = Objects.requireNonNull(bool);
    }

    public JsonPrimitive(Number number) {
        this.object = Objects.requireNonNull(number);
    }

    public JsonPrimitive(String string) {
        this.object = Objects.requireNonNull(string);
    }

    @Override
    public JsonElement deepCopy() {
        return null;
    }

    @Override
    public boolean getAsBoolean() {
        return switch (object) {
            case Boolean bool -> bool;

            case Number number -> switch (number.intValue()) {
                case 0 -> false;
                case 1 -> true;
                default -> throw new IllegalArgumentException("Invalid boolean value.");
            };

            case String string -> switch (string.toUpperCase()) {
                case "FALSE" -> false;
                case "TRUE" -> true;
                default -> throw new IllegalArgumentException("Invalid boolean value.");
            };

            default -> throw new AssertionError(String.format("Unexpected value type: '%s'.", object.getClass()));
        };
    }

    @Override
    public Number getAsNumber() {
        if (object instanceof Number number) {
            return number;
        }

        throw new IllegalStateException("Not a JSON Number.");
    }

    @Override
    public String getAsString() {
        return switch (object) {
            case String string -> string;
            case Number number -> String.valueOf(number);
            case Boolean bool -> String.valueOf(bool);
            default -> throw new AssertionError(String.format("Unexpected value type: '%s'.", object.getClass()));
        };
    }

    @Override
    public UUID getAsUUID() {
        if (object instanceof String string) {
            return UUID.fromString(string);
        }

        throw new IllegalStateException("Not a JSON String.");
    }

    @Override
    public <T extends Enum<T>> Enum<T> getAsEnum(Class<T> enumerator) {
        if (object instanceof String string) {
            return Enum.valueOf(enumerator, string);
        }

        throw new IllegalStateException("Not a JSON Number.");
    }

    @Override
    public BigDecimal getAsBigDecimal() {
        if (object instanceof BigDecimal decimal) {
            return decimal;
        }

        throw new IllegalStateException("Not a JSON Number with instance of BigDecimal.");
    }
}
