package nl.radiantrealm.library.utils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class FormatUtils {

    private FormatUtils() {}

    /**
     * Formats a {@link String} into a {@link BigDecimal}, utilizing the {@link Result} wrapper.
     *
     * @param string Input value to format into a {@link BigDecimal}.
     * @return A {@link Result} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Result<BigDecimal> parseBigDecimal(String string) {
        try {
            return new Result<>(
                    Optional.of(new BigDecimal(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    public static Result<Boolean> parseBoolean(String string) {
        try {
            return new Result<>(
                    Optional.of(Boolean.parseBoolean(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    /**
     * Formats a {@link String} into a {@link Integer}, utilizing the {@link Result} wrapper.
     *
     * @param string Input value to format into a {@link Integer}.
     * @return A {@link Result} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Result<Integer> parseInteger(String string) {
        try {
            return new Result<>(
                    Optional.of(Integer.valueOf(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }

    /**
     * Formats a {@link String} into a {@link UUID}, utilizing the {@link Result} wrapper.
     *
     * @param string Input value to format into a {@link UUID}.
     * @return A {@link Result} wrapper containing either the formatted result or an exception on failure.
     * */
    public static Result<UUID> parseUUID(String string) {
        try {
            return new Result<>(
                    Optional.of(UUID.fromString(string)),
                    Optional.empty()
            );
        } catch (Exception e) {
            return new Result<>(
                    Optional.empty(),
                    Optional.of(e)
            );
        }
    }
}
