package nl.radiantrealm.library.processor;

public interface ProcessHandler<IN, OUT> {
    ProcessResult<OUT> handle(ProcessRequest<IN> request) throws Exception;
}
