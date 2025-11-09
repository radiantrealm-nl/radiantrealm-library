package nl.radiantrealm.library.http.enumerator;

import java.util.HashMap;
import java.util.Map;

public enum MediaType {
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

    private static final Map<String, MediaType> map = new HashMap<>();

    static {
        for (MediaType mediaType : MediaType.values()) {
            map.put(mediaType.type, mediaType);
        }
    }

    public static MediaType valueOfType(String type) {
        return map.get(type);
    }

    public final String type;

    MediaType(String type) {
        this.type = type;
    }
}
