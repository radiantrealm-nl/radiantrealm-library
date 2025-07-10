package nl.radiantrealm.library.utils;

import java.math.BigDecimal;
import java.util.UUID;

public class FormatUtiis {

    private FormatUtiis() {}

    /**
     * Tries to parse a {@link String} into a {@link BigDecimal}.
     *
     * @param string            the String to parse from
     * @return the parsed value, or null on failure
     * */
    public static BigDecimal parseBigDecimal(String string) {
        try {
            return new BigDecimal(string);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to parse a {@link String} into an {@link Integer}.
     *
     * @param string            the String to parse from
     * @return the parsed value, or null on failure.
     * */
    public static Integer parseInteger(String string) {
        try {
            return Integer.valueOf(string);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to parse a {@link String} into a {@link UUID}.
     *
     * @param string            the String to parse from
     * @return the parsed value, or null on failure.
     * */
    public static UUID parseUUID(String string) {
        try {
            return UUID.fromString(string);
        } catch (Exception e) {
            return null;
        }
    }
}
