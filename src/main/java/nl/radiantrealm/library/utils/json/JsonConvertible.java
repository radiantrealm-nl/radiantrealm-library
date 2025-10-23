package nl.radiantrealm.library.utils.json;

import java.lang.reflect.InvocationTargetException;

public interface JsonConvertible {
    JsonObject toJson();

    static <T> T fromJson(Class<T> type, JsonObject object) throws Exception {
        try {
            return type.getConstructor(JsonObject.class).newInstance(object);
        } catch (InstantiationException | IllegalArgumentException | InvocationTargetException e) {
            throw new Exception(String.format("Failed to instance class for '%s'.", type.getSimpleName()), e);
        }
    }
}
