package nl.radiantrealm.library.net.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FragmentedFrame {
    private final Queue<WebsocketFrame> queue = new ConcurrentLinkedQueue<>();

    public WebsocketFrame glue(WebsocketFrame lastFrame) {
        WebsocketFrame result = null;

        synchronized (queue) {
            if (lastFrame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
                queue.add(lastFrame);

                if (lastFrame.finalMessage()) {
                    List<WebsocketFrame> list = new ArrayList<>(queue.size());
                    long payloadLength = 0;

                    for (WebsocketFrame frame : queue) {
                        if (list.isEmpty() && frame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
                            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid frame fragmentation"));
                        } else if (!list.isEmpty() && !frame.operatorCode().equals(WebsocketOperatorCode.CONTINUE)) {
                            throw new WebsocketException(WebsocketExitCode.PROTOCOL_ERROR.generateFrame("Invalid frame fragmentation"));
                        }

                        list.add(queue.poll());
                        payloadLength += frame.payload().length;

                        if (payloadLength > Integer.MAX_VALUE) {
                            throw new WebsocketException(WebsocketExitCode.MESSAGE_TOO_BIG.generateFrame());
                        }

                        if (frame.finalMessage()) {
                            break;
                        }
                    }

                    byte[] resultBytes = new byte[(int) payloadLength];
                    int index = 0;

                    for (WebsocketFrame frame : list) {
                        byte[] payloadBytes = frame.payload();
                        System.arraycopy(payloadBytes, 0, resultBytes, index, payloadBytes.length);
                        index += payloadBytes.length;
                    }

                    result = new WebsocketFrame(
                            list.getFirst().operatorCode(),
                            true,
                            resultBytes
                    );
                }
            } else if (lastFrame.finalMessage()) {
                result = lastFrame;
            } else {
                queue.add(lastFrame);
            }
        }

        return result;
    }
}
