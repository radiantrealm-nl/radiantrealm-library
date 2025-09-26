package nl.radiantrealm.library.utils.dto;

import nl.radiantrealm.library.utils.function.ThrowingBiFunction;
import nl.radiantrealm.library.utils.function.ThrowingFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public record Result<T>(Optional<T> object, Optional<Exception> exception) {

    public static <T> Result<T> ok(T object) {
        return new Result<>(Optional.of(object), Optional.empty());
    }

    public static <T> Result<T> error(Exception exception) {
        return new Result<>(Optional.empty(), Optional.of(exception));
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

    public static <T, R> R function(Function<T, R> function, T arg1) {
        return function.apply(arg1);
    }

    public static <T, U, R> R function(BiFunction<T, U, R> function, T arg1, U arg2) {
        return function.apply(arg1, arg2);
    }

    public static <T, R> R tryFunction(ThrowingFunction<T, R> function, T arg1) throws Exception {
        return function.apply(arg1);
    }

    public static <T, U, R> R tryFunction(ThrowingBiFunction<T, U, R> function, T arg1, U arg2) throws Exception {
        return function.apply(arg1, arg2);
    }

    public static <T, R> Result<R> tryCatch(ThrowingFunction<T, R> function, T arg1) {
        try {
            return Result.ok(function.apply(arg1));
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    public static <T, U, R> Result<R> tryCatch(ThrowingBiFunction<T, U, R> function, T arg1, U arg2) {
        try {
            return Result.ok(function.apply(arg1, arg2));
        } catch (Exception e) {
            return Result.error(e);
        }
    }
}
