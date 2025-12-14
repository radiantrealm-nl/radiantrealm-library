package nl.radiantrealm.library.net.io;

import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualByteBuffer {
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger position = new AtomicInteger(0);

    public void duplicate(VirtualByteBuffer buffer) {
        if (buffer != null) {
            buffer.queue.addAll(queue);
            buffer.position.set(position.get());
        }
    }

    public void add(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            queue.add(bytes);
        }
    }

    public byte[] peek() {
        return queue.peek();
    }

    public byte[] poll() {
        return queue.poll();
    }

    public void clear() {
        queue.clear();
        position.set(0);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int available() {
        int result = 0;

        for (byte[] bytes : queue) {
            result += bytes.length;
        }

        return result - position.get();
    }

    public Byte read() {
        byte[] bytes = queue.peek();

        if (bytes == null) {
            return null;
        }

        return bytes[position.get()];
    }

    public byte[] read(int length) {
        int available = available();

        if (length < 1 || available == 0) {
            return new byte[0];
        }

        byte[] result = new byte[Math.min(available, length)];
        boolean firstIteration = true;
        int index = 0;

        for (byte[] bytes : queue) {
            int position = firstIteration ? this.position.get() : 0;
            int read = Math.min(result.length - index, bytes.length - position);

            if (firstIteration) {
                firstIteration = false;
            }

            if (read > 0) {
                System.arraycopy(bytes, position, result, index, read);
                index += read;

                if (index == result.length) {
                    break;
                }
            }
        }

        return result;
    }

    public Byte consume() {
        byte[] bytes = queue.peek();

        if (bytes == null) {
            return null;
        }

        byte b = bytes[position.getAndIncrement()];

        if (position.get() == bytes.length) {
            queue.poll();
            position.set(0);
        }

        return b;
    }

    public byte[] consume(int length) {// 1. Read the data (allocates array and copies data)
        // This uses your existing read() method which does NOT move the position.
        byte[] data = read(length);

        // 2. If we actually got data, advance the pointers
        if (data.length > 0) {
            remove(data.length);
        }

        return data;
    }

    public void remove(int length) {
        if (length <= 0 || queue.isEmpty()) {
            return;
        }

        int remaining = length;

        // Loop until we have consumed enough OR the queue runs dry
        while (remaining > 0 && !queue.isEmpty()) {
            byte[] head = queue.peek();

            // Safety check: if a null array got into the queue somehow
            if (head == null) {
                queue.poll();
                continue;
            }

            int currentPos = position.get();
            int availableInHead = head.length - currentPos;

            // We can consume either what's left in this chunk, or just what we need
            int toEat = Math.min(remaining, availableInHead);

            // Update loop state
            remaining -= toEat;

            // Advance the pointers
            // If we reached the end of the current array, remove it.
            if (currentPos + toEat >= head.length) {
                queue.poll();
                position.set(0); // Reset position for the new head
            } else {
                // Otherwise, just move the cursor forward
                position.addAndGet(toEat);
            }
        }
    }

    public int scan(byte[] sequence) {
        if (sequence == null || sequence.length == 0) {
            return -1;
        }

        int resultIndex = 0;
        int matchIndex = 0;
        boolean firstIteration = true;

        for (byte[] bytes : queue) {
            int position = firstIteration ? this.position.get() : 0;
            firstIteration = false;

            for (int i = position; i < bytes.length; i++) {
                resultIndex++;

                if (bytes[i] == sequence[matchIndex]) {
                    matchIndex++;

                    if (matchIndex == sequence.length) {
                        return resultIndex - sequence.length;
                    }
                } else {
                    matchIndex = 0;
                }
            }
        }

        return -1;
    }

    public int scan(String sequence) {
        return scan(sequence.getBytes(StandardCharsets.UTF_8));
    }
}
