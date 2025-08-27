package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.utils.DataObject;

public interface ProcessType {
    <T extends DataObject<T>> ProcessHandler<T> getHandler();
    Class<? extends DataObject<?>> dto();
}
