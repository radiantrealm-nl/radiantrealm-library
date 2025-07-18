package nl.radiantrealm.library.processor;

public interface ProcessResultListener<T> {
    void onProcessResult(ProcessResult<T> result);
}
