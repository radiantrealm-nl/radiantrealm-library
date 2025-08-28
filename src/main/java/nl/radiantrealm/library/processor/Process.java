package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public record Process(int processID, ProcessHandler handler, JsonObject object, Consumer<ProcessResult> consumer) {}
