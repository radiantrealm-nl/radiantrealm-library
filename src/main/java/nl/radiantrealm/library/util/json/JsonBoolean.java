package nl.radiantrealm.library.util.json;

public class JsonBoolean implements JsonPrimitive {
    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);
    public final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }

    public static JsonBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
