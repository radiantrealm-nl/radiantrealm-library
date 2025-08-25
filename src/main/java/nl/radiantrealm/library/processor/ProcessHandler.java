package nl.radiantrealm.library.processor;

public interface ProcessHandler {
    ProcessResult handle(ProcessRequest request) throws Exception;
}
