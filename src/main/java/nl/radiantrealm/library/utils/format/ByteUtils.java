package nl.radiantrealm.library.utils.format;

public class ByteUtils {

    public static boolean getBit(byte value, int index) {
        return (value & index) != 0;
    }

    public static int readBits(byte value, int index) {
        return (value & index);
    }

    public static Byte setBit(byte value, int index, boolean state) {
        if (state) {
            return (byte) (value | (1 << index));
        } else {
            return (byte) (value & ~(1 << index));
        }
    }

    public static byte[] combineBytes(byte... bytes) {
        byte[] result = new byte[bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return result;
    }

    public static int combineBytesInt(byte... bytes) {
        int result = 0;

        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }

        return result;
    }

    public static long combineBytesLong(byte... bytes) {
        long result = 0;

        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }

        return result;
    }

    public static byte[] toBytes(int value) {
        byte[] result = new byte[4];

        for (int i = 0; i < 4; i++) {
            result[3 - i] = (byte) (value >> (i * 8));
        }

        return result;
    }

    public static byte[] toBytes(long value) {
        byte[] result = new byte[8];

        for (int i = 0; i < 8; i++) {
            result[7 - i] = (byte) (value >> (i * 8));
        }

        return result;
    }
}
