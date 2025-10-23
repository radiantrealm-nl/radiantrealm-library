package nl.radiantrealm.library.processor;

import com.sun.net.httpserver.HttpExchange;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.http.model.HttpTools;
import nl.radiantrealm.library.utils.json.JsonConvertible;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.util.Optional;

public record ProcessResult(boolean success, int code, Optional<JsonObject> object, Optional<Exception> exception) implements JsonConvertible {

    @Override
    public JsonObject toJson() {
        return toJson(true);
    }

    public JsonObject toJson(boolean includeSuccessCode) {
        JsonObject jsonObject = object.orElse(new JsonObject());

        if (includeSuccessCode) {
            jsonObject.add("success", success);
            jsonObject.add("code", code);
        }

        return jsonObject;
    }

    public void sendHttpResponse(HttpExchange exchange) throws Exception {
        new HttpTools(exchange).sendResponse(
                code,
                MediaType.JSON.type,
                toJson(false).toString()
        );
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
        jsonObject.add("error", error);

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
        jsonObject.add("error", error);

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
