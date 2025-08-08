package nl.radiantrealm.library.enumerator;

public enum MimeType {
    TEXT("text/plain", 0x01),
    HTML("text/html", 0x02),
    CSS("text/css", 0x03),
    JS("text/js", 0x04),

    JSON("application/json", 0x05),
    XML("application/xml", 0x06),
    PDF("application/pdf", 0x07),
    ZIP("application/zip", 0x08),
    BINARY("application/octet-stream", 0x09),

    JPEG("image/jpeg", 0x0A),
    PNG("image/png", 0x0B),
    GIF("image/gif", 0x0C),
    SVG("image/svg+xml", 0x0D),

    MP3("audio/mpeg", 0x0E),
    OGG("audio/ogg", 0x0F),
    WAV("audio/wav", 0x10),

    MP4("video/mp4", 0x11),
    WEBM("video/webm", 0x12);

    private final String type;
    private final int hex;

    MimeType(String type, int hex) {
        this.type = type;
        this.hex = hex;
    }

    public String getType() {
        return type;
    }

    public int getHex() {
        return hex;
    }

    public static MimeType getHex(int hex) {
        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.hex == hex) {
                return mimeType;
            }
        }

        return null;
    }
}
