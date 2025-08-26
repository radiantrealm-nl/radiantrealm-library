package nl.radiantrealm.library.utils;

import java.util.Optional;

public record Result<T>(Optional<T> object, Optional<Exception> exception) {

    public static <T> Result<T> ok(T object) {
        return new Result<>(Optional.of(object), Optional.empty());
    }

    public static <T> Result<T> error(Exception exception) {
        return new Result<>(Optional.empty(), Optional.of(exception));
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

    public Exception getError() {
        return exception.orElse(null);
    }

    public boolean isObjectEmpty() {
        return object.isEmpty();
    }

    public void throwIt() throws Exception {
        if (exception.isPresent()) {
            throw exception.get();
        }
    }
}
