package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.io.VirtualByteBuffer;

import java.nio.ByteBuffer;

public class AggregatedWebsocketParser {
    private final WebsocketSession session;

    private WebsocketOperatorCode operatorCode = null;
    private Boolean finalMessage = null;
    private Boolean isMasked = null;
    private int initialPayloadLength = -1;
    private long payloadLength = -1;
    private byte[] bitmask = null;

    public AggregatedWebsocketParser(WebsocketSession session) {
        this.session = session;
    }

    public synchronized WebsocketFrame parse() {
        VirtualByteBuffer inboundBuffer = session.inboundBuffer;

        synchronized (inboundBuffer) {
            if (initialPayloadLength == -1) {
                if (inboundBuffer.available() < 2) {
                    return null;
                }

                byte[] bytes = inboundBuffer.consume(2);
                operatorCode = WebsocketOperatorCode.valueOfCode(bytes[0] & 0x0F);

                if (operatorCode == null) {
                    throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame());
                }

                finalMessage = (bytes[0] & 0x80) != 0;
                isMasked = (bytes[1] & 0x80) != 0;
                initialPayloadLength = bytes[1] & 0x7F;
            }

            if (payloadLength == -1) {
                if (initialPayloadLength < 126) {
                    payloadLength = initialPayloadLength;
                } else if (initialPayloadLength == 126) {
                    if (inboundBuffer.available() < 2) {
                        return null;
                    }

                    byte[] bytes = inboundBuffer.consume(2);
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    payloadLength = Short.toUnsignedInt(buffer.getShort());
                } else if (initialPayloadLength == 127) {
                    if (inboundBuffer.available() < 8) {
                        return null;
                    }

                    byte[] bytes = inboundBuffer.consume(8);
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    payloadLength = buffer.getLong();
                }

                if (payloadLength > Integer.MAX_VALUE) {
                    throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG.generateFrame());
                }
            }

            if (isMasked && bitmask == null) {
                if (inboundBuffer.available() < 4) {
                    return null;
                }

                bitmask = inboundBuffer.consume(4);
            }

            if (inboundBuffer.available() < payloadLength) {
                return null;
            }

            byte[] payload = inboundBuffer.consume((int) payloadLength);

            if (payload.length != payloadLength) {
                throw new WebsocketException(WebsocketExitCode.INVALID_PAYLOAD_DATA.generateFrame());
            }

            if (isMasked) {
                for (int i = 0; i < payloadLength; i++) {
                    payload[i] ^= bitmask[i % 4];
                }
            }

            WebsocketFrame frame = new WebsocketFrame(
                    operatorCode,
                    finalMessage,
                    payload
            );

            operatorCode = null;
            finalMessage = null;
            isMasked = null;
            initialPayloadLength = -1;
            payloadLength = -1;
            bitmask = null;
            return frame;
        }
    }
}
