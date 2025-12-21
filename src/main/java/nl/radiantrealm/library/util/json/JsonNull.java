package nl.radiantrealm.library.util.json;

public class JsonNull implements JsonPrimitive {
    public static final JsonNull INSTANCE = new JsonNull();
    private static final String content = "null";

    private JsonNull() {}

    @Override
    public String toString() {
        return content;
    }
}
