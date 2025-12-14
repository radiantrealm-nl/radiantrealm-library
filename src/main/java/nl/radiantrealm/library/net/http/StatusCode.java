package nl.radiantrealm.library.net.http;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    PARTIAL_CONTENT(206, "Partial Content"),

    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INVALID_METHOD(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    REQUEST_TIMEOUT(429, "Too Many Requests"),

    SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout");

    private static final Map<Integer, StatusCode> map = new HashMap<>();

    static {
        for (StatusCode statusCode : StatusCode.values()) {
            map.put(statusCode.code, statusCode);
        }
    }

    public static StatusCode valueOfCode(int code) {
        return map.get(code);
    }

    public final int code;
    public final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
