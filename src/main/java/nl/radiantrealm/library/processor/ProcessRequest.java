package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;

public record ProcessRequest(int processID, ProcessType processType, JsonObject object, ProcessResultCallback callback) {

    public String operationalClassName() {
        return processType.getHandler().getClass().getSimpleName();
    }
}
