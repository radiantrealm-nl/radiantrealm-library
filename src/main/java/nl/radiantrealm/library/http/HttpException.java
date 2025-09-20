package nl.radiantrealm.library.http;

import nl.radiantrealm.library.utils.ThrowingBiFunction;
import nl.radiantrealm.library.utils.ThrowingFunction;

public class HttpException extends Exception {
    public final HttpResponse response;

    public HttpException(HttpResponse response) {
        this.response = response;
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
