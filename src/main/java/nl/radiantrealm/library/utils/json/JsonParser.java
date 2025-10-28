package nl.radiantrealm.library.utils.json;

import java.math.BigDecimal;

public class JsonParser {

    private JsonParser() {}

    public static JsonElement parse(String input) {
        return new Parser(input).parse();
    }

    public static JsonObject parseJsonObject(String input) {
        return new Parser(input).parseJsonObject();
    }

    public static JsonArray parseJsonArray(String input) {
        return new Parser(input).parseJsonArray();
    }

    private static class Parser {
        private final String input;
        private int index = 0;

        public Parser(String input) {
            this.input = input;
        }

        public JsonElement parse() {
            return switch (advance(true)) {
                case '{' -> getJsonObject(new JsonObject());
                case '[' -> getJsonArray(new JsonArray());
                default -> throw new JsonException("Not a Json object or Json array");
            };
        }

        public JsonObject parseJsonObject() {
            if (advance(true) != '{') {
                throw new JsonException("Not a Json object");
            }

            return getJsonObject(new JsonObject());
        }

        private JsonObject getJsonObject(JsonObject object) {
            char c = advance(true);

            return switch (c) {
                case '"' -> {
                    String key = getJsonString(new StringBuilder());

                    if ((c = advance(true)) != ':') {
                        throw unexpectedCharacter(c, "Expected ':'.");
                    }

                    switch (c = advance(true)) {
                        case '{' -> object.add(key, getJsonObject(new JsonObject()));
                        case '[' -> object.add(key, getJsonArray(new JsonArray()));
                        case '"' -> object.add(key, getJsonString(new StringBuilder(String.valueOf(c))));
                        case 't', 'f' -> object.add(key, getJsonBoolean(c == 't'));
                        case 'n' -> object.add(key, getJsonNull());
                        case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> object.add(key, getJsonNumber(c));
                        default -> throw unexpectedCharacter(c, "Expected Json element.");
                    }

                    yield getJsonObject(object);
                }

                case ',' -> getJsonObject(object);
                case '}' -> object;
                default -> throw unexpectedCharacter(c, "Expected new Json element or '}'");
            };
        }

        public JsonArray parseJsonArray() {
            if (advance(true) != '[') {
                throw new JsonException("Not a Json array");
            }

            return getJsonArray(new JsonArray());
        }

        private JsonArray getJsonArray(JsonArray array) {
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
                default -> throw unexpectedCharacter(c, "Expected new Json element or ']'");
            };
        }

        private String getJsonString(StringBuilder builder) {
            char c = advance(false);

            return switch (c) {
                case '\\' -> switch (c = advance(false)) {
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
                            hexBuilder.append(switch (c = advance(false)) {
                                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'A',
                                     'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F' -> c;
                                default -> throw new IllegalArgumentException("Invalid hex value");
                            });
                        }

                        yield hexBuilder.toString();
                    }

                    default -> String.format("\\%s", c);
                };

                case '"' -> builder.toString();
                default -> getJsonString(builder.append(c));
            };
        }

        private boolean getJsonBoolean(boolean state) {
            StringBuilder builder = new StringBuilder(state ? "t" : "f");

            for (int i = 0; i< (state ? 3 : 4); i++) {
                char c = peek(false);

                builder.append(switch (c) {
                    case 'r', 'u', 'e', 'a', 'l', 's' -> {
                        advance(false);
                        yield c;
                    }

                    default -> throw new JsonException("oei");
                });
            }

            return switch (builder.toString()) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new IllegalArgumentException("Invalid boolean value");
            };
        }

        private JsonNull getJsonNull() {
            StringBuilder builder = new StringBuilder("n");

            for (int i = 0; i < 3; i++) {
                char c = peek(false);

                builder.append(switch (c) {
                    case 'u', 'l' -> {
                        advance(false);
                        yield c;
                    }

                    default -> throw new JsonException("piep");
                });
            }

            if (builder.toString().equals("null")) {
                return JsonNull.INSTANCE;
            } else {
                throw new IllegalArgumentException("Invalid null value");
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

        private JsonException unexpectedCharacter(char c, String reason) {
            return new JsonException(String.format("Unexpected character '%s' at position %s. %s", c, index, reason));
        }
    }
}
