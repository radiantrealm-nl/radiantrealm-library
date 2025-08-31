package nl.radiantrealm.library.utils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FormatUtils {

    private FormatUtils() {}

    public static BigDecimal formatBigDecimal(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for BigDecimal cannot be null or empty.");

        return new BigDecimal(string);
    }

    public static Boolean formatBoolean(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for boolean cannot be null or empty.");

        return switch (string.toUpperCase()) {
            case "FALSE" -> false;
            case "TRUE" -> true;
            default -> throw new IllegalArgumentException(String.format("Invalid boolean value '%s'.", string));
        };
    }

    public static Boolean formatBoolean(Integer integer) throws IllegalArgumentException {
        if (integer == null) throw new IllegalArgumentException("Input value for boolean cannot be null or empty.");

        return switch (integer) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new IllegalArgumentException(String.format("Invalid boolean value '%d'.", integer));
        };
    }

    public static <T extends Enum<T>> T formatEnum(Class<T> enumerator, String string) throws IllegalArgumentException, ClassCastException {
        if (enumerator == null) throw new ClassCastException("Enumerator class cannot be null.");
        if (string == null) throw new IllegalArgumentException("Input value for enum cannot be null or empty..");

        return Enum.valueOf(enumerator, string);
    }

    public static <T extends Enum<T>> T formatEnum(Class<T> enumerator, ResultSet resultSet, String key) throws IllegalArgumentException, ClassCastException, SQLException {
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

    public static UUID formatUUID(String string) throws IllegalArgumentException {
        if (string == null) throw new IllegalArgumentException("Input value for UUID cannot be null or empty.");

        return UUID.fromString(string);
    }

    public static UUID formatUUID(ResultSet resultSet, String key) throws SQLException {
        return formatUUID(resultSet.getString(key));
    }
}
