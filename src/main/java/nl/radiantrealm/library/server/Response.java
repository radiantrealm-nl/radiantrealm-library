package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Optional;

public record Response(int statusCode, Optional<JsonObject> object) {

    public static Response ok() {
        return new Response(200, Optional.empty());
    }

    public static Response ok(Map<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        map.forEach(jsonObject::addProperty);
        return new Response(200, Optional.of(jsonObject));
    }

    public static Response error(int statusCode, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", message);
        return new Response(statusCode, Optional.of(jsonObject));
    }

    public JsonObject getBody() {
        return object.orElse(null);
    }
}
