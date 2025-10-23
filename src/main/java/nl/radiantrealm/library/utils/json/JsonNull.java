package nl.radiantrealm.library.utils.json;

public class JsonNull extends JsonElement {
    public static final JsonNull INSTANCE = new JsonNull();

    @Override
    public JsonElement deepCopy() {
        return INSTANCE;
    }

    @Override
    protected String toString(StringBuilder builder, boolean prettyPrint) {
        return "null";
    }
}
