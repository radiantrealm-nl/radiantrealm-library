package nl.radiantrealm.library.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.InvocationTargetException;

public interface DataObject {
    Gson gson = new Gson();

    default JsonObject toJson() throws IllegalStateException {
        return gson.toJsonTree(this.getClass()).getAsJsonObject();
    }

    static <T extends DataObject> T fromJson(Class<T> clazz, JsonObject object) throws Exception {
        try {
            return clazz.getConstructor(JsonObject.class).newInstance(object);
        } catch (NoSuchMethodException e) {
            return gson.fromJson(object, clazz);
        } catch (InvocationTargetException | InstantiationException | IllegalArgumentException e) {
            throw new Exception("Failed to instance class for " + clazz.getSimpleName(), e);
        }
    }
}
