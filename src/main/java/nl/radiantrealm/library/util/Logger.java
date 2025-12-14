package nl.radiantrealm.library.util;

import java.io.PrintWriter;
import java.io.StringWriter;
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
        WARNING,
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
        StringBuilder builder = new StringBuilder(message);
        builder.append('\n');
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        cause.printStackTrace(printer);
        builder.append(writer);
        log(level, builder.toString());
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void info(String message, Throwable cause) {
        log(Level.INFO, message, cause);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void warning(String message, Throwable cause) {
        log(Level.WARNING, message, cause);
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
