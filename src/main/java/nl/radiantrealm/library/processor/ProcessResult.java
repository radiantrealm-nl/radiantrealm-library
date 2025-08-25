package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;

public record ProcessResult(int processID, boolean success, JsonObject object) {}
