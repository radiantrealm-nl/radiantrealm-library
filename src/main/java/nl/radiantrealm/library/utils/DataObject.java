package nl.radiantrealm.library.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface DataObject<T> {
    Gson gson = new Gson();

    Class<T> getType();

    @SuppressWarnings("unchecked")
    default T cast(Object object) throws Exception {
        if (this.getClass().isInstance(object)) {
            return (T) object;
        }

        throw new ClassCastException("Object cannot be cast to " + this.getClass().getName());
    }

    default T fromJson(JsonObject object) throws Exception {
        return gson.fromJson(object, getType());
    }

    default JsonObject toJson(T object) throws Exception {
        return gson.toJsonTree(object).getAsJsonObject();
    }
}
