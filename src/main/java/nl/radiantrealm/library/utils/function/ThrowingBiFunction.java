package nl.radiantrealm.library.utils.function;

public interface ThrowingBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;
}
