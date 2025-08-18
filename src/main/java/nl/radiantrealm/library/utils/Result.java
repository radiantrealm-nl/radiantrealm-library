package nl.radiantrealm.library.utils;

import java.util.Optional;

public record Result<T>(Optional<T> object, Optional<Throwable> throwable) {

    public static <T> Result<T> ok(T object) {
        return new Result<>(Optional.of(object), Optional.empty());
    }

    public static <T> Result<T> error(Throwable throwable) {
        return new Result<>(Optional.empty(), Optional.of(throwable));
    }

    public static <T> Result<T> tryCatch(ThrowingFunction<T> throwingFunction) {
        try {
            return Result.ok(throwingFunction.apply());
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    public static <T> Result<T> tryWithResources(AutoCloseable autoCloseable, ThrowingFunction<T> throwingFunction) {
        try (autoCloseable) {
            return tryCatch(throwingFunction);
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    public interface ThrowingFunction<R> {
        R apply() throws Exception;
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
