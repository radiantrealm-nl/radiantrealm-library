package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.util.json.JsonObject;

import java.util.function.Consumer;

public record Process(
        int processID,
        ProcessHandler handler,
        JsonObject object,
        Consumer<ProcessResult> consumer
) {}
