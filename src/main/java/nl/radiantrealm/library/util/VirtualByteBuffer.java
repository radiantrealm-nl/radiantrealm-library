package nl.radiantrealm.library.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VirtualByteBuffer {
    private final Queue<Byte> queue = new ConcurrentLinkedQueue<>();

    public void add(byte b) {
        queue.add(b);
    }

    public void add(byte[] bytes) {
        for (byte b : bytes) {
            queue.add(b);
        }
    }

    public void add(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        add(bytes);
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public byte peek() {
        return queue.peek();
    }

    public byte[] peek(int length) {
        if (length == 0 || queue.isEmpty()) {
            return new byte[0];
        }

        byte[] bytes = new byte[Math.min(queue.size(), length)];

        int index = 0;
        for (byte b : queue) {
            if (bytes.length == index) {
                return bytes;
            }

            bytes[index] = b;
            index++;
        }

        return bytes;
    }

    public byte poll() {
        return queue.poll();
    }

    public byte[] poll(int length) {
        if (length == 0 || queue.isEmpty()) {
            return new byte[0];
        }

        byte[] bytes = new byte[Math.min(queue.size(), length)];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = queue.poll();
        }

        return bytes;
    }

    public int scan(byte[] sequence) {
        if (sequence.length < 1 || queue.isEmpty()) {
            return -1;
        }

        int matchIndex = 0;
        int queueIndex = 0;

        for (byte b : queue) {
            queueIndex++;

            if (b == sequence[matchIndex]) {
                matchIndex++;

                if (sequence.length == matchIndex) {
                    return queueIndex - sequence.length;
                }
            } else {
                matchIndex = 0;
            }
        }

        return -1;
    }

    public int scan(String sequence) {
        return scan(sequence.getBytes(StandardCharsets.UTF_8));
    }
}
