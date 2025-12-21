package nl.radiantrealm.library.util.json;

public class JsonFormatException extends JsonException {

    public JsonFormatException() {
        super();
    }

    public JsonFormatException(String message) {
        super(message);
    }

    public JsonFormatException(Throwable cause) {
        super(cause);
    }

    public JsonFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
