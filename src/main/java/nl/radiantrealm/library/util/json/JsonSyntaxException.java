package nl.radiantrealm.library.util.json;

public class JsonSyntaxException extends JsonException {

    public JsonSyntaxException() {
        super();
    }

    public JsonSyntaxException(String message) {
        super(message);
    }

    public JsonSyntaxException(Throwable cause) {
        super(cause);
    }

    public JsonSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
