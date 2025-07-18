package nl.radiantrealm.library.processor;

import java.util.Optional;

public record ProcessResult<T>(Optional<T> object, Optional<Throwable> throwable) {

    public static <T> ProcessResult<T> success(T object) {
        return new ProcessResult<>(Optional.of(object), Optional.empty());
    }

    public static <T> ProcessResult<T> failure(Throwable throwable) {
        return new ProcessResult<>(Optional.empty(), Optional.of(throwable));
    }

    public T getObject() {
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
