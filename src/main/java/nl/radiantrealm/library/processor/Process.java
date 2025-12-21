package nl.radiantrealm.library.processor;

import java.util.concurrent.CompletableFuture;

public record Process(
        ProcessHandler handler,
        CompletableFuture<ProcessResult> future
) {}
