package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.util.Optional;

public record ProcessResult(boolean success, Optional<JsonObject> object, Optional<Exception> exception) {

    public String getErrorMessage() throws IllegalArgumentException {
        return object.map(jsonObject -> JsonUtils.getJsonString(jsonObject, "error")).orElse(null);
    }

    public static ProcessResult ok() {
        return new ProcessResult(
                true,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ProcessResult error(String error) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        return new ProcessResult(
                false,
                Optional.of(jsonObject),
                Optional.empty()
        );
    }

    public static ProcessResult error(String error, Exception e) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        return new ProcessResult(
                false,
                Optional.of(jsonObject),
                Optional.of(e)
        );
    }
}
