package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Optional;

public record ApiResponse(int statusCode, Optional<JsonObject> object) {

    public static ApiResponse ok() {
        return new ApiResponse(200, Optional.empty());
    }

    public static ApiResponse ok(Map<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        map.forEach(jsonObject::addProperty);
        return new ApiResponse(200, Optional.of(jsonObject));
    }

    public static ApiResponse error(int statusCode, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", message);
        return new ApiResponse(statusCode, Optional.of(jsonObject));
    }

    public JsonObject getBody() {
        return object.orElse(null);
    }
}
