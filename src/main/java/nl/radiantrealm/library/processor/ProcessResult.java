package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.net.http.StatusCode;
import nl.radiantrealm.library.util.json.JsonObject;

public record ProcessResult(
        StatusCode statusCode,
        JsonObject object
) {
    public static ProcessResult ok(JsonObject object) {
        return ok(StatusCode.OK, object);
    }

    public static ProcessResult ok(StatusCode statusCode, JsonObject object) {
        return new ProcessResult(
                statusCode,
                object
        );
    }

    public static ProcessResult error() {
        return error(StatusCode.SERVER_ERROR, "Server error");
    }

    public static ProcessResult error(String message) {
        return error(StatusCode.SERVER_ERROR, message);
    }

    public static ProcessResult error(StatusCode statusCode, String message) {
        JsonObject object = new JsonObject();
        object.put("error", message);

        return new ProcessResult(
                statusCode,
                object
        );
    }
}
