package nl.radiantrealm.library.util.json;

public record JsonPrintingContext(
        boolean prettyPrint,
        int tabDepth
) {
    public static final JsonPrintingContext defaultContext = new JsonPrintingContext(
            true,
            4
    );
}
