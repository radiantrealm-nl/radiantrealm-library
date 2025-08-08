package nl.radiantrealm.library.utils;

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

    public static int combineBytes(byte... bytes) {
        int result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}
