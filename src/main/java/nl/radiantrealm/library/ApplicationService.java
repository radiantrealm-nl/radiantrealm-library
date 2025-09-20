package nl.radiantrealm.library;

import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public interface ApplicationService {
    AtomicBoolean isRunning = new AtomicBoolean(false);
    AtomicLong runningSince = new AtomicLong(System.currentTimeMillis());

    default String serviceType() {
        return "Unknown service type.";
    }

    default void start() throws Exception {
        isRunning.set(true);
        runningSince.set(System.currentTimeMillis());
    }

    default void stop() throws Exception {
        isRunning.set(false);
    }

    default JsonObject status() throws Exception {
        JsonObject object = new JsonObject();
        object.addProperty("running_since", runningSince.get());
        return object;
    }

    default void command(JsonObject object) throws Exception {
        throw new IllegalArgumentException("Not implemented.");
    }
}
