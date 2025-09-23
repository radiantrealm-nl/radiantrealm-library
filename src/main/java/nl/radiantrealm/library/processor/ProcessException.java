package nl.radiantrealm.library.processor;

public class ProcessException extends Exception {
    public final ProcessResult result;

    public ProcessException(ProcessResult result) {
        this.result = result;
    }
}
