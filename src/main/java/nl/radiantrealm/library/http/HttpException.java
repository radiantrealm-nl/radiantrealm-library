package nl.radiantrealm.library.http;

import nl.radiantrealm.library.http.context.HttpResponseContext;
import nl.radiantrealm.library.utils.function.ThrowingBiFunction;
import nl.radiantrealm.library.utils.function.ThrowingFunction;
import nl.radiantrealm.library.utils.function.ThrowingNilFunction;

public class HttpException extends RuntimeException {
    public final HttpResponseContext responseContext;

    public HttpException(HttpResponseContext responseContext) {
        this.responseContext = responseContext;
    }

    public HttpException(Exception e, HttpResponseContext responseContext) {
        super(e);
        this.responseContext = responseContext;
    }

    public static <R> R tryFunction(ThrowingNilFunction<R> function, HttpResponseContext responseContext) {
        try {
            return function.apply();
        } catch (Exception e) {
            throw new HttpException(e, responseContext);
        }
    }

    public static <T, R> R tryFunction(ThrowingFunction<T, R> function, T arg1, HttpResponseContext responseContext) {
        try {
            return function.apply(arg1);
        } catch (Exception e) {
            throw new HttpException(e, responseContext);
        }
    }

    public static <T, U, R> R tryFunction(ThrowingBiFunction<T, U, R> function, T arg1, U arg2, HttpResponseContext responseContext) {
        try {
            return function.apply(arg1, arg2);
        } catch (Exception e) {
            throw new HttpException(e, responseContext);
        }
    }
}
