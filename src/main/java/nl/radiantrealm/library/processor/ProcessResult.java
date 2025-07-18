package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Optional;

public record ProcessResult(boolean success, Optional<JsonObject> object, Optional<Throwable> throwable) {

    public static ProcessResult ok() {
        return new ProcessResult(true, Optional.empty(), Optional.empty());
    }

    public static ProcessResult ok(Map<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        map.forEach(jsonObject::addProperty);
        return new ProcessResult(true, Optional.of(jsonObject), Optional.empty());
    }

    public static ProcessResult failure(Throwable throwable) {
        return new ProcessResult(false, Optional.empty(), Optional.of(throwable));
    }

    public JsonObject getJsonObject() {
        return object.orElse(null);
    }

    public Throwable getThrowable() {
        return throwable.orElse(null);
    }

    public void throwIt() throws Throwable {
        if (throwable.isPresent()) {
            throw throwable.get();
        }
    }
}
