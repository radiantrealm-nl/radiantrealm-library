package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.nio.ByteBuffer;

public class AggregatedWebsocketFrame {
    private final WebsocketSession session;

    private WebsocketOperatorCode operatorCode = null;
    private Boolean finalMessage = null;
    private Boolean isMasked = null;
    private int initialPayloadLength = -1;
    private long payloadLength = -1;
    private byte[] bitmask = null;

    public AggregatedWebsocketFrame(WebsocketSession session) {
        this.session = session;
    }

    public synchronized WebsocketFrame parse() {
        VirtualByteBuffer inboundBuffer = session.inboundBuffer;

        synchronized (inboundBuffer) {
            if (initialPayloadLength == -1) {
                if (inboundBuffer.size() < 2) {
                    return null;
                }

                byte[] bytes = inboundBuffer.poll(2);
                this.operatorCode = WebsocketOperatorCode.valueOfCode(bytes[0] & 0x0F);

                if (operatorCode == null) {
                    throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame());
                }

                this.finalMessage = (bytes[0] & 0x80) != 0;
                this.isMasked = (bytes[1] & 0x80) != 0;
                this.initialPayloadLength = bytes[1] & 0x7F;
            }

            if (payloadLength == -1) {
                if (initialPayloadLength < 126) {
                    this.payloadLength = initialPayloadLength;
                } else {
                    if (initialPayloadLength == 126) {
                        if (inboundBuffer.size() < 2) {
                            return null;
                        }

                        byte[] bytes = inboundBuffer.poll(2);
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        this.payloadLength = Short.toUnsignedInt(buffer.getShort());
                    }

                    if (initialPayloadLength == 127) {
                        if (inboundBuffer.size() < 8) {
                            return null;
                        }

                        byte[] bytes = inboundBuffer.poll(8);
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        this.payloadLength = buffer.getLong();
                    }
                }

                if (payloadLength > Integer.MAX_VALUE) {
                    throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG.generateFrame());
                }
            }

            if (isMasked && bitmask == null) {
                if (inboundBuffer.size() < 4) {
                    return null;
                }

                this.bitmask = inboundBuffer.poll(4);
            }

            if (inboundBuffer.size() < payloadLength) {
                return null;
            }

            byte[] payload = inboundBuffer.poll((int) payloadLength);

            if (isMasked) {
                for (int i = 0; i < payload.length; i++) {
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
