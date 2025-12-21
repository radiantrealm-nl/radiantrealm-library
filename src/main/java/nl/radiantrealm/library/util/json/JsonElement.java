package nl.radiantrealm.library.util.json;

public interface JsonElement {

    default String prettyPrint() {
        return prettyPrint(2, 0);
    }

    default String prettyPrint(int depth, int offset) {
        return toString();
    }
}
