package nl.radiantrealm.library.util.function;

public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
