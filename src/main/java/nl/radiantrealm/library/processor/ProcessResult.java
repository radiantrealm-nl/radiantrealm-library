package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.utils.JsonUtils;

import java.util.Optional;

public record ProcessResult(boolean success, int code, Optional<JsonObject> object, Optional<Exception> exception) {

    public String getErrorMessage() throws IllegalArgumentException {
        return object.map(jsonObject -> JsonUtils.getJsonString(jsonObject, "error")).orElse(null);
    }

    public JsonObject toJson() {
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

    public static ProcessResult error(StatusCode code, String error) {
        return error(code.code, error);
    }

    public static ProcessResult error(StatusCode code, String error, Exception e) {
        return error(code.code, error, e);
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
}
