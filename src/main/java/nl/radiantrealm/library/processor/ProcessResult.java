package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.dto.DataObject;

import java.util.Optional;

public record ProcessResult(boolean success, int code, Optional<JsonObject> object, Optional<Exception> exception) implements DataObject {

    @Override
    public JsonObject toJson() throws IllegalStateException {
        JsonObject jsonObject = object.orElse(new JsonObject());
        jsonObject.addProperty("success", success);
        jsonObject.addProperty("code", code);
        return jsonObject;
    }

    public static ProcessResult ok() {
        return new ProcessResult(
                true,
                200,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ProcessResult ok(JsonObject object) {
        return new ProcessResult(
                true,
                200,
                Optional.of(object),
                Optional.empty()
        );
    }

    public static ProcessResult error(int code, String error) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        return new ProcessResult(
                false,
                code,
                Optional.of(jsonObject),
                Optional.empty()
        );
    }

    public static ProcessResult error(StatusCode statusCode, String error) {
        return error(statusCode.code, error);
    }

    public static ProcessResult error(int code, String error, Exception e) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        return new ProcessResult(
                false,
                code,
                Optional.of(jsonObject),
                Optional.of(e)
        );
    }

    public static ProcessResult error(StatusCode statusCode, String error, Exception e) {
        return error(statusCode.code, error, e);
    }
}
