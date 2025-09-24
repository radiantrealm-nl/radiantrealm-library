package nl.radiantrealm.library.processor;

public class ProcessException extends Exception {
    public final ProcessResult result;

    public ProcessException(ProcessResult result) {
        this.result = result;
    }

    public static <T, R> R tryFunction(ThrowingFunction<T, R> function, T arg1, HttpResponse response) throws Exception {
        try {
            return function.apply(arg1);
        } catch (IllegalArgumentException e) {
            throw new HttpException(response);
        }
    }

    public static <T, U, R> R tryFunction(ThrowingBiFunction<T, U, R> function, T arg1, U arg2, HttpResponse response) throws Exception {
        try {
            return function.apply(arg1, arg2);
        } catch (IllegalArgumentException e) {
            throw new HttpException(response);
        }
    }
}
