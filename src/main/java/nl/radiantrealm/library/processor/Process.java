package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public record Process(int processID, ProcessHandler handler, JsonObject object, Consumer<ProcessResult> consumer) {

    public ProcessResult handle() throws Exception {
        return handler.handle(this);
    }

    public void callback(ProcessResult result) {
        if (consumer != null) {
            consumer.accept(result);
        }
    }
}
