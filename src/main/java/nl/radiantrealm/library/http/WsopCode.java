package nl.radiantrealm.library.http;

public enum WsopCode {
    CONTINUATION(0x00),
    CLOSE(0x01),
    PING(0x02),
    PONG(0x03),

    MIME_TEXT(0x80, MimeType.TEXT),
    MIME_HTML(0x81, MimeType.HTML),
    MIME_CSS(0x82, MimeType.CSS),
    MIME_JS(0x83, MimeType.JS),

    MIME_JSON(0x84, MimeType.JSON),
    MIME_XML(0x85, MimeType.XML),
    MIME_PDF(0x86, MimeType.PDF),
    MIME_ZIP(0x87, MimeType.ZIP),
    MIME_BINARY(0x88, MimeType.BINARY),

    MIME_JPEG(0x89, MimeType.JPEG),
    MIME_PNG(0x8A, MimeType.PNG),
    MIME_GIF(0x8B, MimeType.GIF),
    MIME_SVG(0x8C, MimeType.SVG),

    MIME_MP3(0x8D, MimeType.MP3),
    MIME_OGG(0x8E, MimeType.OGG),
    MIME_WAV(0x8F, MimeType.WAV),

    MIME_MP4(0x90, MimeType.MP4),
    MIME_WEBM(0x91, MimeType.WEBM);

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
