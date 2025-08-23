package nl.radiantrealm.library;

import java.util.concurrent.atomic.AtomicBoolean;

public interface ApplicationService {
    AtomicBoolean isRunning = new AtomicBoolean(false);

    default void start() {
        isRunning.set(true);
    }

    default void stop() {
        isRunning.set(false);
    }

    default boolean isRunning() {
        return isRunning.get();
    }
}
