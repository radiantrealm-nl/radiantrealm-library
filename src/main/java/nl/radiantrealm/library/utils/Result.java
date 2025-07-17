package nl.radiantrealm.library.utils;

import java.util.Optional;

public record Result<T>(Optional<T> object, Optional<Throwable> throwable) {

    public static <T> Result<T> ok(T object) {
        return new Result<>(Optional.of(object), Optional.empty());
    }

    public static <T> Result<T> error(Throwable throwable) {
        return new Result<>(Optional.empty(), Optional.of(throwable));
    }

    public T getObject() {
        return object.orElse(null);
    }

    public Throwable getThrowable() {
        return throwable.orElse(null);
    }

    public boolean isObjectEmpty() {
        return object.isEmpty();
    }

    public void throwIt() throws Throwable {
        if (throwable.isPresent()) {
            throw throwable.get();
        }
    }
}
