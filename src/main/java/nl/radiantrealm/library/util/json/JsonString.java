package nl.radiantrealm.library.util.json;

import java.util.Objects;

public record JsonString(String value) implements JsonPrimitive {

    public JsonString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static String escapeCharacters(String value) {
        StringBuilder builder = new StringBuilder(value.length());

        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> builder.append("\\\\");
                case '\"' -> builder.append("\\\"");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < ' ' || c > '~') {
                        builder.append(String.format(
                                "\\u%04X",
                                (int) c
                        ));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "\"%s\"",
                escapeCharacters(value)
        );
    }
}
