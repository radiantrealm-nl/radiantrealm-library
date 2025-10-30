package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.json.JsonObject;

public record ProcessResult(int statusCode, JsonObject object) {

    public static ProcessResult ok() {
        return new ProcessResult(200, new JsonObject());
    }

    public static ProcessResult ok(JsonObject object) {
        return new ProcessResult(200, object);
    }

    public static ProcessResult error(StatusCode statusCode) {
        return new ProcessResult(statusCode.code, new JsonObject());
    }

    public static ProcessResult error(StatusCode statusCode, JsonObject object) {
        return new ProcessResult(statusCode.code, object);
    }
}
