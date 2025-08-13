package nl.radiantrealm.library.http;

import com.sun.net.httpserver.HttpExchange;

public record HttpRequest(HttpExchange exchange) implements AutoCloseable {

    @Override
    public void close() {
        exchange.close();
    }

    public String getRequestBody()

//    public String getRequestBody(InputStream stream) {
//        return new BufferedReader(new InputStreamReader(stream))
//                .lines()
//                .collect(Collectors.joining("\n"));
//    }
//
//    public String getRequestBody() {
//        return getRequestBody(exchange.getRequestBody());
//    }
//
//    public void sendResponse(int statusCode, String mimeType, String body) throws Exception {
//        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
//
//        setResponseHeader("Content-Type", mimeType);
//        exchange.sendResponseHeaders(statusCode, bytes.length);
//
//        OutputStream stream = exchange.getResponseBody();
//        stream.write(bytes);
//        stream.close();
//    }
//
//    public void sendResponse(StatusCode statusCode, MimeType mimeType, String body) throws Exception {
//        sendResponse(statusCode.getCode(), mimeType.getType(), body);
//    }
//
//    public void sendResponse(StatusCode statusCode, JsonObject object) throws Exception {
//        sendResponse(statusCode, MimeType.JSON, object.toString());
//    }
//
//    public void sendStatusResponse(StatusCode statusCode, String key, String value) throws Exception {
//        JsonObject object = new JsonObject();
//        object.addProperty(key, value);
//        sendResponse(statusCode, object);
//    }
//
//    public void sendStatusResponse(StatusCode statusCode) throws Exception {
//        sendStatusResponse(statusCode, statusCode.getKeyType(), statusCode.getMessage());
//    }
//
//    public void setResponseHeader(String key, String value) {
//        exchange.getResponseHeaders().set(key, value);
//    }
}
