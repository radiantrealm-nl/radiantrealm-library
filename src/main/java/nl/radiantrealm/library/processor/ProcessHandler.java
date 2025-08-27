package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.utils.DataObject;

public interface ProcessHandler<T extends DataObject<T>> {
    ProcessResult handle(Process<T> process) throws Exception;
}
