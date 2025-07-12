package nl.radiantrealm.library.server;

import com.google.gson.JsonObject;

import java.util.Optional;

public record Response(int statusCode, Optional<JsonObject> object) {}
