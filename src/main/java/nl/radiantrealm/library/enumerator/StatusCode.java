package nl.radiantrealm.library.enumerator;

public enum StatusCode {
    CONTINUE(100, null),
    SWITCHING_PROTOCOLS(101, null),
    PROCESSING(102, null),

    OK(200, "Ok."),
    CREATED(201, "Created."),
    ACCEPTED(202, "Accepted."),
    NO_CONTENT(204, null),
    PARTIAL_CONTENT(206, "Partial_content."),

    MOVED_PERMANENTLY(301, "Permanently moved."),
    FOUND(302, "Found."),
    NOT_MODIFIED(304, null),
    TEMPORARY_REDIRECT(307, "Temporary redirect."),
    PERMANENT_REDIRECT(308, "Permanent redirect."),

    BAD_REQUEST(400, "Bad request body."),
    UNAUTHORIZED(401, "Insufficient permissions."),
    FORBIDDEN(403, "No access."),
    NOT_FOUND(404, "The request resource was not found."),
    INVALID_METHOD(405, "Invalid request method."),
    CONFLICT(409, "Conflicting request body."),
    REQUEST_TIMEOUT(429, "Too many requests."),

    SERVER_ERROR(500, "A server error ocurred."),
    NOT_IMPLEMENTED(501, "Method not implemented."),
    BAD_GATEWAY(502, "Bad gateway."),
    SERVICE_UNAVAILABLE(503, "Service not available."),
    GATEWAY_TIMEOUT(504, "Gateway timeout.");

    public final int code;
    public final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getKeyType() {
        if (message == null) {
            return null;
        }

        if (code >= 200 && code < 300) {
            return "message";
        }

        if (code >= 300 && code < 400) {
            return "redirect";
        }

        if (code >= 400 && code < 600) {
            return "error";
        }

        return "info";
    }
}
