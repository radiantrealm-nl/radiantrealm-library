package nl.radiantrealm.library.util.function;

public interface ThrowingNilFunction<R> {
    R apply() throws Exception;
}
