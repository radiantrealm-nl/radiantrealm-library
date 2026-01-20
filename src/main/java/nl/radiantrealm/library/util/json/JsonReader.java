package nl.radiantrealm.library.util.json;

import java.math.BigDecimal;
import java.util.Stack;

public class JsonReader {
    private final char[] chars;
    private int position = 0;

    public JsonReader(String string) {
        chars = string.toCharArray();
    }

    private char nextChar(boolean consume) {
        int position = this.position;

        while (position < chars.length && Character.isWhitespace(chars[position])) {
            position++;
        }

        if (position >= chars.length) {
            throw new JsonSyntaxException("Unexpected end of JSON input");
        }

        this.position = position + (consume ? 1 : 0);
        return chars[position];
    }

    public static JsonElement parse(String string) {
        JsonReader reader = new JsonReader(string);
        return reader.parse();
    }

    public JsonElement parse() {
        Stack<JsonContainer> containerStack = new Stack<>();
        JsonElement rootElement = parseJsonElement();

        if (rootElement instanceof JsonContainer container) {
            containerStack.push(container);
        }

        while (!containerStack.isEmpty()) {
            switch (containerStack.peek()) {
                case JsonArray array -> {
                    if (nextChar(false) == ']') {
                        nextChar(true);
                        containerStack.pop();

                        if (!containerStack.isEmpty() && nextChar(false) == ',') {
                            nextChar(true);
                        }
                    } else {
                        switch (parseJsonElement()) {
                            case JsonContainer container -> {
                                containerStack.push(container);
                                array.add(container);
                            }

                            case JsonPrimitive primitive -> {
                                array.add(primitive);

                                switch (nextChar(true)) {
                                    case ']' -> containerStack.pop();

                                    case ',' -> {
                                        if (nextChar(false) == ']') {
                                            throw new JsonSyntaxException(String.format("Unexpected end of array at position %s", position));
                                        }
                                    }

                                    default -> throw new JsonSyntaxException(String.format("Unexpected character at position %s, expected ',' or ']'", position));
                                }
                            }

                            default -> throw new JsonSyntaxException("Invalid JSON structure");
                        }
                    }
                }

                case JsonObject object -> {
                    if (nextChar(false) == '}') {
                        nextChar(true);
                        containerStack.pop();

                        if (!containerStack.isEmpty() && nextChar(false) == ',') {
                            nextChar(true);
                        }
                    } else if (parseJsonElement() instanceof JsonString string) {
                        if (nextChar(true) != ':') {
                            throw new JsonSyntaxException(String.format("Unexpected character at position %s, expected ':'", position));
                        }

                        switch (parseJsonElement()) {
                            case JsonContainer container -> {
                                containerStack.push(container);
                                object.put(string.value(), container);
                            }

                            case JsonPrimitive primitive -> {
                                object.put(string.value(), primitive);

                                switch (nextChar(true)) {
                                    case '}' -> containerStack.pop();

                                    case ',' -> {
                                        if (nextChar(false) == '}') {
                                            throw new JsonSyntaxException(String.format("Unexpected end of object at position %s", position));
                                        }
                                    }
                                }
                            }

                            default -> throw new JsonSyntaxException("Invalid JSON structure");
                        }
                    }
                }

                default -> throw new JsonSyntaxException("Invalid JSON structure");
            }
        }

        return rootElement;
    }

    private JsonElement parseJsonElement() {
        char firstChar = nextChar(true);

        return switch (firstChar) {
            case '[' -> new JsonArray();
            case '{' -> new JsonObject();

            case 't', 'f' -> {
                int length = firstChar == 't' ? 4 : 5;
                position += length - 1;

                if (chars.length <= position) {
                    throw new JsonSyntaxException("Unexpected end of JSON input");
                }

                String string = new String(chars, position - length, length);
                yield switch (string) {
                    case "true" -> JsonBoolean.TRUE;
                    case "false" -> JsonBoolean.FALSE;
                    default -> throw new JsonFormatException(String.format("Invalid JSON boolean value '%s'", string));
                };
            }

            case 'n' -> {
                position += 3;

                if (chars.length <= position) {
                    throw new JsonSyntaxException("Unexpected end of JSON input");
                }

                String string = new String(chars, position - 4, 4);
                if (string.equals("null")) {
                    yield JsonNull.INSTANCE;
                } else {
                    throw new JsonFormatException(String.format("Invalid JSON null value '%s'", string));
                }
            }

            case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
                int start = position - 1;

                while (chars.length > position) {
                    char c = chars[position];

                    if ((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                        position++;
                    } else {
                        String string = new String(chars, start, position - start);

                        try {
                            yield new JsonNumber(new BigDecimal(string));
                        } catch (NumberFormatException e) {
                            throw new JsonFormatException(String.format("Unexpected JSON number value '%s'", string));
                        }
                    }
                }

                throw new JsonSyntaxException("Unexpected end of JSON input");
            }

            case '"' -> {
                int start = position;
                int length = 0;

                while (chars.length > position) {
                    char c = chars[position++];

                    if (c == '\\') {
                        if (chars.length <= position) {
                            throw new JsonSyntaxException("Unexpected end of JSON input");
                        }

                        length++;
                        position += switch (chars[position]) {
                            case '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> 1;
                            case 'u' -> 4;
                            default -> throw new JsonFormatException(String.format("Invalid JSON unicode '%s'", chars[position]));
                        };
                    } else if (c == '"') {
                        char[] resultChars = new char[length];
                        int writeIndex = 0;

                        while (writeIndex < resultChars.length) {
                            c = chars[start++];

                            if (c == '\\') {
                                resultChars[writeIndex++] = switch (chars[start++]) {
                                    case '"' -> '"';
                                    case '\\' -> '\\';
                                    case '/' -> '/';
                                    case 'b' -> '\b';
                                    case 'f' -> '\f';
                                    case 'n' -> '\n';
                                    case 'r' -> '\r';
                                    case 't' -> '\t';

                                    case 'u' -> {
                                        int value = Integer.parseInt(new String(chars, start, 4));
                                        start += 4;
                                        yield (char) value;
                                    }

                                    default -> throw new JsonFormatException(String.format("Invalid JSON unicode '%s'", c));
                                };
                            } else {
                                resultChars[writeIndex++] = c;
                            }
                        }

                        yield new JsonString(new String(resultChars));
                    } else {
                        length++;
                    }
                }

                throw new JsonSyntaxException("Unexpected end of JSON input");
            }

            default -> throw new JsonSyntaxException(String.format("Unknown start of JSON element at position %s", position));
        };
    }
}
