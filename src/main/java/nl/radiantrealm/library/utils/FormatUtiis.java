package nl.radiantrealm.library.utils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class FormatUtiis {

    private FormatUtiis() {}

    /**
     * Formats a {@link String} into a {@link BigDecimal}, utilizing the {@link Parsable} wrapper.
     *
     * @param string Input value to format into a {@link BigDecimal}.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Parsable<BigDecimal> parseBigDecimal(String string) {
        try {
            return new Parsable<>(
                    Optional.of(new BigDecimal(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    /**
     * Formats a {@link String} into a {@link Integer}, utilizing the {@link Parsable} wrapper.
     *
     * @param string Input value to format into a {@link Integer}.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Parsable<Integer> parseInteger(String string) {
        try {
            return new Parsable<>(
                    Optional.of(Integer.valueOf(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    /**
     * Formats a {@link String} into a {@link UUID}, utilizing the {@link Parsable} wrapper.
     *
     * @param string Input value to format into a {@link UUID}.
     * @return A {@link Parsable} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Parsable<UUID> parseUUID(String string) {
        try {
            return new Parsable<>(
                    Optional.of(UUID.fromString(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Parsable<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }
}
