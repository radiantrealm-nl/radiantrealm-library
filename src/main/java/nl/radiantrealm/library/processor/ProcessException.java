package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.utils.function.ThrowingBiFunction;
import nl.radiantrealm.library.utils.function.ThrowingFunction;
import nl.radiantrealm.library.utils.function.ThrowingNilFunction;

public class ProcessException extends RuntimeException {
    public final ProcessResult result;

    public ProcessException(ProcessResult result) {
        this.result = result;
    }

    public ProcessException(Exception e, ProcessResult result) {
        super(e);
        this.result = result;
    }

    public static <R> R tryFunction(ThrowingNilFunction<R> function, ProcessResult result) {
        try {
            return function.apply();
        } catch (Exception e) {
            throw new ProcessException(e, result);
        }
    }

    public static <T, R> R tryFunction(ThrowingFunction<T, R> function, T arg1, ProcessResult result) {
        try {
            return function.apply(arg1);
        } catch (Exception e) {
            throw new ProcessException(e, result);
        }
    }

    public static <T, U, R> R tryFunction(ThrowingBiFunction<T, U, R> function, T arg1, U arg2, ProcessResult result) {
        try {
            return function.apply(arg1, arg2);
        } catch (Exception e) {
            throw new ProcessException(e, result);
        }
    }
}
