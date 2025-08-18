package nl.radiantrealm.library.http;

public enum HttpMethod {
    GET("get"),
    HEAD("head"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    CONNECT("connect"),
    OPTIONS("options"),
    TRACE("trace"),
    PATCH("patch");

    public final String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public static HttpMethod getMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }
}
