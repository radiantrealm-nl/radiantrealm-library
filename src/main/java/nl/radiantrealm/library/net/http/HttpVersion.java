package nl.radiantrealm.library.net.http;

public enum HttpVersion {
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1"),
    HTTP_2("HTTP/2"),
    HTTP_3("HTTP/3");

    public static HttpVersion getVersion(String string) {
        return switch (string) {
            case "HTTP/1.0" -> HTTP_1_0;
            case "HTTP/1.1" -> HTTP_1_1;
            case "HTTP/2" -> HTTP_2;
            case "HTTP/3" -> HTTP_3;
            default -> null;
        };
    }

    public final String version;

    HttpVersion(String version) {
        this.version = version;
    }
}
