package nl.radiantrealm.library.util.json;

public class JsonInstanceException extends JsonException {

    public JsonInstanceException() {
        super();
    }

    public JsonInstanceException(String message) {
        super(message);
    }

    public JsonInstanceException(Throwable cause) {
        super(cause);
    }

    public JsonInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
