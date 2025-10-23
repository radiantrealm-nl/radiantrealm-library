package nl.radiantrealm.library.utils.json;

import java.math.BigDecimal;

public class JsonParser {
    private final String input;
    private int index;

    public JsonParser(String input) {
        this.input = input;
    }

    public static JsonObject getJsonObject(String json) {
        return new JsonParser(json).getJsonObject();
    }

    public static JsonArray getJsonArray(String json) {
        return new JsonParser(json).getJsonArray();
    }

    public JsonObject getJsonObject() {
        if (advance(true) == '{') {
            return getJsonObject(new JsonObject());
        } else {
            throw new JsonException("Not a Json Object.");
        }
    }

    public JsonObject getJsonObject(JsonObject object) {
        char c = advance(true);

        return switch (c) {
            case '"' -> {
                String key = getJsonString(new StringBuilder());

                c = advance(true);

                if (c != ':') {
                    throw new JsonException(String.format("Unexpected character '%s' at position %s", c, index));
                }

                c = advance(true);

                switch (c) {
                    case '{' -> object.add(key, getJsonObject(new JsonObject()));
                    case '[' -> object.add(key, getJsonArray(new JsonArray()));
                    case '"' -> object.add(key, getJsonString(new StringBuilder()));
                    case 't', 'f' -> object.add(key, getJsonBoolean(c == 't'));
                    case 'n' -> object.add(key, getJsonNull());
                    case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> object.add(key, getJsonNumber(c));
                    default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", c, index));
                }

                yield getJsonObject(object);
            }

            case ',' -> getJsonObject(object);
            case '}' -> object;
            default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", c, index));
        };
    }

    public JsonArray getJsonArray() {
        if (advance(true) == '[') {
            return getJsonArray(new JsonArray());
        } else {
            throw new JsonException("Not a Json Object.");
        }
    }

    public JsonArray getJsonArray(JsonArray array) {
        char c = advance(true);

        return switch (c) {
            case '{' -> {
                array.add(getJsonObject(new JsonObject()));
                yield getJsonArray(array);
            }

            case '[' -> {
                array.add(getJsonArray(new JsonArray()));
                yield getJsonArray(array);
            }

            case '"' -> {
                array.add(getJsonString(new StringBuilder()));
                yield getJsonArray(array);
            }

            case 't', 'f' -> {
                array.add(getJsonBoolean(c == 't'));
                yield getJsonArray(array);
            }

            case 'n' -> {
                array.add(getJsonNull());
                yield getJsonArray(array);
            }

            case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
                array.add(getJsonNumber(c));
                yield getJsonArray(array);
            }

            case ',' -> getJsonArray(array);
            case ']' -> array;
            default -> throw new JsonException(String.format("Unexpected character '%s' at position %s (expected '\"' or '}')", c, index));
        };
    }

    private String getJsonString(StringBuilder builder) {
        char c = advance(false);

        return switch (c) {
            case '\\' -> {
                c = advance(false);

                yield switch (c) {
                    case '"' -> "\"";
                    case '\\' -> "\\";
                    case '/' -> "/";
                    case 'b' -> "\b";
                    case 'n' -> "\n";
                    case 'r' -> "\r";
                    case 't' -> "\t";
                    case 'f' -> "\f";

                    case 'u' -> {
                        StringBuilder hexBuilder = new StringBuilder("u");

                        for (int i = 0; i < 4; i++) {
                            hexBuilder.append(advance(false));
                        }

                        yield hexBuilder.toString();
                    }

                    default -> "\\" + c;
                };
            }

            case '"' -> builder.toString();
            default -> getJsonString(builder.append(c));
        };
    }

    private boolean getJsonBoolean(boolean state) {
        StringBuilder builder = new StringBuilder(state ? "t" : "f");

        for (int i = 0; i < (state ? 3 : 4); i++) {
            char c = peek(false);

            switch (c) {
                case 'r', 'u', 'e', 'a', 'l', 's' -> {
                    advance(false);
                    builder.append(c);
                }

                default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", c, index));
            }
        }

        return switch (builder.toString()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean value.");
        };
    }

    private JsonNull getJsonNull() {
        StringBuilder builder = new StringBuilder("n");

        for (int i = 0; i < 3; i++) {
            char c = peek(false);

            switch (c) {
                case 'u', 'l' -> {
                    advance(false);
                    builder.append(c);
                }

                default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", c, index));
            }
        }

        if (builder.toString().equals("null")) {
            return JsonNull.INSTANCE;
        } else {
            throw new IllegalArgumentException("Invalid null value.");
        }
    }

    private Number getJsonNumber(char firstChar) {
        return new BigDecimal(getJsonNumber(new StringBuilder(switch (firstChar) {
            case '-' -> {
                char c = peek(false);

                int i = Character.getNumericValue(c);

                yield switch (i) {
                    case 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 -> {
                        yield String.valueOf(i);
                    }

                    default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", c, index));
                };
            }

            case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
                int i = Character.getNumericValue(firstChar);

                yield switch (i) {
                    case 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 -> {
                        yield String.valueOf(i);
                    }

                    default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", firstChar, index));
                };
            }
            default -> throw new JsonException(String.format("Unexpected character '%s' at position %s", firstChar, index));
        })));
    }

    private String getJsonNumber(StringBuilder builder) {
        char c = peek(false);

        return switch (c) {
            case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.', 'e', 'E', '+', '-' -> {
                advance(true);
                yield getJsonNumber(builder.append(c));
            }

            default -> builder.toString();
        };
    }

    private char peek(boolean skipWhiteSpace) {
        int peekIndex = index;

        if (skipWhiteSpace) {
            while (peekIndex < input.length() && Character.isWhitespace(input.charAt(peekIndex))) {
                peekIndex++;
            }
        }

        if (peekIndex >= input.length()) {
            throw new ArrayIndexOutOfBoundsException(String.format("Unexpected end of Json input at position %s", index));
        }

        return input.charAt(peekIndex);
    }

    private char advance(boolean skipWhiteSpace) {
        while (index < input.length()) {
            char c = input.charAt(index);

            if (skipWhiteSpace && Character.isWhitespace(c)) {
                index++;
            } else {
                index++;
                return c;
            }
        }

        throw new ArrayIndexOutOfBoundsException(String.format("Unexpected end of Json input at position %s", index));
    }
}
