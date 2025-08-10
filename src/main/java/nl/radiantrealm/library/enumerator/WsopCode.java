package nl.radiantrealm.library.enumerator;

import java.util.HashMap;
import java.util.Map;

public enum WsopCode {
    CONTINUATION(0x00),
    TEXT_UTF8(0x01),
    BINARY(0x02),
    MIME_TYPE(0x03),
    CLOSE(0x08),
    PING(0x09),
    PONG(0x0A),

    MIME_TEXT(0x10, MimeType.TEXT),
    MIME_HTML(0x11, MimeType.HTML),
    MIME_CSS(0x12, MimeType.CSS),
    MIME_JS(0x13, MimeType.JS),

    MIME_JSON(0x14, MimeType.JSON),
    MIME_XML(0x15, MimeType.XML),
    MIME_PDF(0x16, MimeType.PDF),
    MIME_ZIP(0x17, MimeType.ZIP),
    MIME_BINARY(0x18, MimeType.BINARY),

    MIME_JPEG(0x19, MimeType.JPEG),
    MIME_PNG(0x1A, MimeType.PNG),
    MIME_GIF(0x1B, MimeType.GIF),
    MIME_SVG(0x1C, MimeType.SVG),

    MIME_MP3(0x1D, MimeType.MP3),
    MIME_OGG(0x1E, MimeType.OGG),
    MIME_WAV(0x1F, MimeType.WAV),

    MIME_MP4(0x20, MimeType.MP4),
    MIME_WEBM(0x21, MimeType.WEBM);

    public final int code;
    public final MimeType mimeType;

    private static final Map<Integer, WsopCode> map = new HashMap<>();

    static {
        for (WsopCode wsopCode : WsopCode.values()) {
            map.put(wsopCode.code, wsopCode);
        }
    }

    WsopCode(int code) {
        this.code = code;
        this.mimeType = null;
    }

    WsopCode(int code, MimeType mimeType) {
        this.code = code;
        this.mimeType = mimeType;
    }

    public static WsopCode getCode(int hex) {
        return map.get(hex);
    }
}
