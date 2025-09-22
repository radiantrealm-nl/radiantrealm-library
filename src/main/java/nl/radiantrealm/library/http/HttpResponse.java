package nl.radiantrealm.library.http;

import com.google.gson.JsonObject;

public record HttpResponse(int statusCode, String mediaType, String responseBody) {

    public HttpResponse(StatusCode statusCode, JsonObject object) {
        this(
                statusCode.code,
                MediaType.JSON.type,
                object.getAsString()
        );
    }

    public HttpResponse(StatusCode statusCode) {
        this(
                statusCode,
                statusCode.toJson()
        );
    }

    public HttpResponse(StatusCode statusCode, String message) {
        this(
                statusCode,
                statusCode.toJson(message)
        );
    }

    public HttpResponse(StatusCode statusCode, String key, String message) {
        this(
                statusCode,
                statusCode.toJson(key, message)
        );
    }
}
