package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;

import java.util.Optional;

public record Request(String method, Optional<JsonObject> body) {

    public JsonObject getBody() {
        return body.orElse(null);
    }
}
