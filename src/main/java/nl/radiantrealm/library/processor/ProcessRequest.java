package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;

public record ProcessRequest(int processID, JsonObject object, ProcessHandler handler, ProcessResultCallback callback) {

    public String operationalClassName() {
        return handler.getClass().getSimpleName();
    }
}
