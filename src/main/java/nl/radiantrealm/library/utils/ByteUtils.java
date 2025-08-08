package nl.radiantrealm.library.utils;

public class ByteUtils {

    public static boolean getBit(byte value, int index) {
        return ((value >> index) & 1) == 1;
    }

    public static Byte setBit(byte value, int index, boolean state) {
        if (state) {
            return (byte) (value | (1 << index));
        } else {
            return (byte) (value & ~(1 << index));
        }
    }

    public static int readBits(byte value, int startBit, int length) {
        int mask = (1 << length) - 1; // bijv. length=4 → mask=0b1111
        return (value >> startBit) & mask;
    }

    public static int combineBytes(byte... bytes) {
        int result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}
