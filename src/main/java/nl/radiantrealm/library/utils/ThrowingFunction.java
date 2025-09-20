package nl.radiantrealm.library.utils;

public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
