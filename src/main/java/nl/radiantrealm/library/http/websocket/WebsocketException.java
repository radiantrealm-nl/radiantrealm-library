package nl.radiantrealm.library.http.websocket;

import nl.radiantrealm.library.utils.function.ThrowingBiFunction;
import nl.radiantrealm.library.utils.function.ThrowingFunction;
import nl.radiantrealm.library.utils.function.ThrowingNilFunction;

public class WebsocketException extends RuntimeException {
    public final WebsocketExitCode exitCode;

    public WebsocketException(WebsocketExitCode exitCode) {
        this.exitCode = exitCode;
    }

    public WebsocketException(Exception e, WebsocketExitCode exitCode) {
        super(e);
        this.exitCode = exitCode;
    }

    public static <R> R tryFunction(ThrowingNilFunction<R> function, WebsocketExitCode exitCode) {
        try {
            return function.apply();
        } catch (Exception e) {
            throw new WebsocketException(e, exitCode);
        }
    }

    public static <T, R> R tryFunction(ThrowingFunction<T, R> function, T arg1, WebsocketExitCode exitCode) {
        try {
            return function.apply(arg1);
        } catch (Exception e) {
            throw new WebsocketException(e, exitCode);
        }
    }

    public static <T, U, R> R tryFunction(ThrowingBiFunction<T, U, R> function, T arg1, U arg2, WebsocketExitCode exitCode) {
        try {
            return function.apply(arg1, arg2);
        } catch (Exception e) {
            throw new WebsocketException(e, exitCode);
        }
    }
}
