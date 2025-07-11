package nl.radiantrealm.library.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final String prefix;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger(String className) {
        this.prefix = className;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public static Logger getLogger(String className) {
        return new Logger(className);
    }

    private String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private enum Level {
        INFO,
        ERROR,
        DEBUG,
    }

    private void log(Level level, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(timestamp()).append("] [");
        builder.append(level.name()).append("] [");
        builder.append(prefix).append("] ");
        builder.append(message);

        System.out.println(builder);
    }

    private void log(Level level, String message, Throwable cause) {
        log(level, message + "\n" + cause);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void info(String message, Throwable cause) {
        log(Level.INFO, message, cause);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void error(String message, Throwable cause) {
        log(Level.ERROR, message, cause);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void debug(String message, Throwable cause) {
        log(Level.DEBUG, message, cause);
    }
}
