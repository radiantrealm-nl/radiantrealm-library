package nl.radiantrealm.library.http;

import com.google.gson.JsonObject;

public record HttpResponse(int statusCode, MimeType mimeType, String responseBody) {

    public HttpResponse(StatusCode statusCode, JsonObject object) {
        this(
                statusCode.code,
                MimeType.JSON,
                object.getAsString()
        );
    }

    public HttpResponse(StatusCode statusCode) {
        this(
                statusCode,
                statusCode.buildObject()
        );
    }

    public HttpResponse(StatusCode statusCode, String message) {
        this(
                statusCode,
                statusCode.buildObject(message)
        );
    }

    public HttpResponse(StatusCode statusCode, String key, String message) {
        this(
                statusCode,
                statusCode.buildObject(key, message)
        );
    }
}
