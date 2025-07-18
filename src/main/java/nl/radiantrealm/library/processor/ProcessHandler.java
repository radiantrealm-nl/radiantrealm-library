package nl.radiantrealm.library.processor;

public abstract class ProcessHandler<T> {

    protected abstract ProcessResult handle(ProcessRequest<T> request);
}
