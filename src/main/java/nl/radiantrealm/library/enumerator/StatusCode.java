package nl.radiantrealm.library.enumerator;

public enum StatusCode {
    CONTINUE(100),
    SWITCHING_PROTOCOLS(101),
    PROCESSING(102),

    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),
    PARTIAL_CONTENT(206),

    MOVED_PERMANENTLY(301),
    FOUND(302),
    NOT_MODIFIED(304),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INVALID_METHOD(405),
    CONFLICT(409),
    REQUEST_TIMEOUT(429),

    SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
