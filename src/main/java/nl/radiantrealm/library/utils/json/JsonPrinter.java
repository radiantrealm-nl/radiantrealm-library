package nl.radiantrealm.library.utils.json;

import java.util.HashSet;
import java.util.Map;

public class JsonPrinter {

    private JsonPrinter() {}

    public static String print(JsonElement element) {
        return rawPrinting(element);
    }

    private static String rawPrinting(JsonElement rootElement) {
        StringBuilder builder = new StringBuilder();

        return switch (rootElement) {
            case JsonArray array -> {
                builder.append('[');
                HashSet<String> strings = new HashSet<>(array.size());

                for (JsonElement element : array) {
                    strings.add(rawPrinting(element));
                }

                builder.append(String.join(",", strings));
                yield builder.append(']').toString();
            }

            case JsonObject object -> {
                builder.append('{');
                HashSet<String> strings = new HashSet<>(object.size());

                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    strings.add(String.format(
                            "\"%s\":%s",
                            entry.getKey(),
                            rawPrinting(entry.getValue())
                    ));
                }

                builder.append(String.join(",", strings));
                yield builder.append('}').toString();
            }

            case JsonPrimitive primitive -> printJsonPrimitive(builder, primitive);
            case JsonNull jsonNull -> builder.append(jsonNull).toString();
            default -> throw new JsonException("Unknown Json element type.");
        };
    }

    public static String print(JsonElement element, boolean prettyPrinting) {
        if (prettyPrinting) {
            return new PrettyPrinter().print(element);
        } else {
            return rawPrinting(element);
        }
    }

    public static String print(JsonElement element, JsonPrintingContext context) {
        return new PrettyPrinter(context).print(element);
    }

    private static class PrettyPrinter {
        private final JsonPrintingContext context;

        public PrettyPrinter() {
            this.context = new JsonPrintingContext(4);
        }

        public PrettyPrinter(JsonPrintingContext context) {
            this.context = context;
        }

        public String print(JsonElement element) {
            return print(element, 0);
        }

        private String print(JsonElement rootElement, int depth) {
            StringBuilder builder = new StringBuilder();

            return switch (rootElement) {
                case JsonArray array -> {
                    builder.append("[\n");
                    HashSet<String> strings = new HashSet<>(array.size());
                    depth++;

                    for (JsonElement element : array) {
                        strings.add(String.format(
                                "%s%s",
                                getDepth(depth),
                                print(element, depth)
                        ));
                    }

                    depth--;
                    builder.append(String.join(",\n", strings));
                    yield builder.append(String.format("\n%s]", getDepth(depth))).toString();
                }

                case JsonObject object -> {
                    builder.append("{\n");
                    HashSet<String> strings = new HashSet<>(object.size());
                    depth++;

                    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                        strings.add(String.format(
                                "%s\"%s\": %s",
                                getDepth(depth),
                                entry.getKey(),
                                print(entry.getValue(), depth)
                        ));
                    }

                    depth--;
                    builder.append(String.join(",\n", strings));
                    yield builder.append(String.format("\n%s}", getDepth(depth))).toString();
                }

                case JsonPrimitive primitive -> printJsonPrimitive(builder, primitive);
                case JsonNull jsonNull -> builder.append(jsonNull).toString();
                default -> throw new JsonException("Unknown Json element type.");
            };
        }

        private String getDepth(int depth) {
            return " ".repeat(Math.max(0, context.tabDepth() * depth));
        }
    }

    private static String printJsonPrimitive(StringBuilder builder, JsonPrimitive primitive) {
        return switch (primitive.object) {
            case Boolean bool -> builder.append(bool).toString();
            case Number number -> builder.append(number).toString();
            case String string -> builder.append(String.format("\"%s\"", string)).toString();
            default -> throw new JsonException("Unknown Json primitive type.");
        };
    }
}
