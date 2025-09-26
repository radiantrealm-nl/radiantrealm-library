package nl.radiantrealm.library.utils.function;

public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
