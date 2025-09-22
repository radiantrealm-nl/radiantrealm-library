package nl.radiantrealm.library.http;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.utils.DataObject;

import javax.security.auth.kerberos.KeyTab;

public enum StatusCode implements DataObject {
    CONTINUE(100, null),
    SWITCHING_PROTOCOLS(101, null),
    PROCESSING(102, null),

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, null),
    PARTIAL_CONTENT(206, "Partial Content"),

    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, null),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    INVALID_METHOD(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    REQUEST_TIMEOUT(429, "Too Many Requests"),

    SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Bad  Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout");

    public final int code;
    public final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String keyType() {
        if (message == null) return null;
        if (code < 300) return "info";
        if (code < 400) return "redirect";
        if (code < 600) return "error";
        return null;
    }

    public JsonObject toJson(String key, String message) {
        JsonObject object = new JsonObject();
        object.addProperty(key, message);
        return object;
    }

    public JsonObject toJson(String message) {
        String key = keyType();

        if (key == null) {
            return new JsonObject();
        }

        return toJson(key, message);
    }

    @Override
    public JsonObject toJson() throws IllegalStateException {
        return toJson(message);
    }
}
