package nl.radiantrealm.library.util.function;

public interface ThrowingBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;
}
