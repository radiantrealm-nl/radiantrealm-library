package nl.radiantrealm.library.utils.function;

public interface ThrowingNilFunction<R> {
    R apply() throws Exception;
}
