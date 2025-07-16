package nl.radiantrealm.library.utils;

import java.util.Optional;

public record Parsable<T>(Optional<T> object, Optional<Throwable> throwable) {

    public T getObject() {
        return object.orElse(null);
    }

    public Throwable getThrowable() {
        return throwable.orElse(null);
    }
}
