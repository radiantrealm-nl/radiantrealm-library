package nl.radiantrealm.library.http.model;

import nl.radiantrealm.library.utils.function.ThrowingBiFunction;
import nl.radiantrealm.library.utils.function.ThrowingFunction;
import nl.radiantrealm.library.utils.function.ThrowingNilFunction;

public class HttpException extends RuntimeException {
    public final HttpResponse response;

    public HttpException(HttpResponse response) {
        this.response = response;
    }

    public HttpException(Exception e, HttpResponse response) {
        super(e);
        this.response = response;
    }

    public static <R> R tryFunction(ThrowingNilFunction<R> function, HttpResponse response) throws Exception {
        try {
            return function.apply();
        } catch (Exception e) {
            throw new HttpException(e, response);
        }
    }

    public static <T, R> R tryFunction(ThrowingFunction<T, R> function, T arg1, HttpResponse response) throws Exception {
        try {
            return function.apply(arg1);
        } catch (Exception e) {
            throw new HttpException(e, response);
        }
    }

    public static <T, U, R> R tryFunction(ThrowingBiFunction<T, U, R> function, T arg1, U arg2, HttpResponse response) throws Exception {
        try {
            return function.apply(arg1, arg2);
        } catch (Exception e) {
            throw new HttpException(e, response);
        }
    }
}
