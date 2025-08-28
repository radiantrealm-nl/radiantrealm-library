package nl.radiantrealm.library.processor;

public interface ProcessHandler {
    ProcessResult handle(Process process) throws Exception;
}
