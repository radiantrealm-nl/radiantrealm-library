package nl.radiantrealm.library.utils.format;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FormatUtils {

    private FormatUtils() {}

    public static BigDecimal formatBigDecimal(Number number) {
        if (number == null) return null;

        if (number instanceof BigDecimal decimal) {
            return decimal;
        }

        return new BigDecimal(number.toString());
    }

    public static boolean formatBoolean(String string) {
        return switch (string.toUpperCase()) {
            case "FALSE" -> false;
            case "TRUE" -> true;
            default -> throw new IllegalArgumentException("Invalid boolean value.");
        };
    }

    public static boolean formatBoolean(int i) {
        return switch (i) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new IllegalArgumentException("Invalid boolean value.");
        };
    }

    public static <T extends Enum<T>> T formatEnum(Class<T> enumerator, String string) {
        return Enum.valueOf(enumerator, string);
    }

    public static UUID formatUUID(ResultSet rs, String key) throws SQLException {
        return UUID.fromString(rs.getString(key));
    }    public static <T extends Enum<T>> T formatEnum(Class<T> enumerator, ResultSet resultSet, String key) throws IllegalArgumentException, ClassCastException, SQLException {
        return formatEnum(enumerator, resultSet.getString(key));
    }

    public static Integer formatInteger(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for integer cannot be null or empty.");

        return Integer.parseInt(string);
    }

    public static Long formatLong(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for long cannot be null or empty.");

        return Long.parseLong(string);
    }

    public static String formatString(Charset charsets, byte[] bytes) throws IllegalArgumentException {
        if (bytes == null) throw new IllegalArgumentException("Input value for bytes cannot be null or empty.");
        if (charsets == null) charsets = StandardCharsets.UTF_8;

        return new String(bytes, charsets);
    }

    public static UUID formatUUID(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for UUID cannot be null or empty.");

        return UUID.fromString(string);
    }
}
