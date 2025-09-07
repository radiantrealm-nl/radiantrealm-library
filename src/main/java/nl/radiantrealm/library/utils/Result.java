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

    public static <T> T tryFunction(ThrowingFunction<T> function) throws Exception {
        return function.apply();
    }

    public static <T> T nullFunction(ThrowingFunction<T> function) {
        try {
            return function.apply();
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T defaultFunction(ThrowingFunction<T> function, T defaultReturn) {
        try {
            return function.apply();
        } catch (Exception e) {
            return defaultReturn;
        }
    }

    public static <T> T function(Function<T> function) {
        return function.apply();
    }

    public interface ThrowingFunction<R> {
        R apply() throws Exception;
    }

    public interface Function<R> {
        R apply();
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
