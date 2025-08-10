package nl.radiantrealm.library.enumerator;

public enum MimeType {
    TEXT("text/plain"),
    HTML("text/html"),
    CSS("text/css"),
    JS("text/js"),

    JSON("application/json"),
    XML("application/xml"),
    PDF("application/pdf"),
    ZIP("application/zip"),
    BINARY("application/octet-stream"),

    JPEG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    SVG("image/svg+xml"),

    MP3("audio/mpeg"),
    OGG("audio/ogg"),
    WAV("audio/wav"),

    MP4("video/mp4"),
    WEBM("video/webm");

    public final String type;

    MimeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
