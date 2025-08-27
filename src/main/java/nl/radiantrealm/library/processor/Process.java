package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.utils.DataObject;

import java.util.function.Consumer;

public record Process<T extends DataObject<T>>(int processID, ProcessHandler<T> handler, T input, Consumer<ProcessResult> consumer) {}
