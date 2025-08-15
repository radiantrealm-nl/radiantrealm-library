package nl.radiantrealm.library.http;

public enum WsopCode {
    CONTINUATION(0x00),
    CLOSE(0x08),
    PING(0x09),
    PONG(0x0A),

    TEXT(0x80, MimeType.TEXT),
    HTML(0x81, MimeType.HTML),
    CSS(0x82, MimeType.CSS),
    JS(0x83, MimeType.JS),

    JSON(0x84, MimeType.JSON),
    XML(0x85, MimeType.XML),
    PDF(0x86, MimeType.PDF),
    ZIP(0x87, MimeType.ZIP),
    BINARY(0x88, MimeType.BINARY),

    JPEG(0x89, MimeType.JPEG),
    PNG(0x8A, MimeType.PNG),
    GIF(0x8B, MimeType.GIF),
    SVG(0x8C, MimeType.SVG),

    MP3(0x8D, MimeType.MP3),
    OGG(0x8E, MimeType.OGG),
    WAV(0x8F, MimeType.WAV),

    MP4(0x90, MimeType.MP4),
    WEBM(0x91, MimeType.WEBM);

    public final int code;
    public final MimeType mimeType;

    WsopCode(int code) {
        this(code, null);
    }

    WsopCode(int code, MimeType mimeType) {
        this.code = code;
        this.mimeType = mimeType;
    }

    public boolean isMimeType() {
        return mimeType != null;
    }
}
