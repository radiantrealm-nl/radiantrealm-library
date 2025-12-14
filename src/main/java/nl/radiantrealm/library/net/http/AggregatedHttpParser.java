package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.net.io.SocketConnection;
import nl.radiantrealm.library.net.io.VirtualByteBuffer;

import java.nio.charset.StandardCharsets;

public class AggregatedHttpParser {
    private final RequestParser requestParser = new RequestParser();
    private final ResponseParser responseParser = new ResponseParser();
    private final SocketConnection connection;

    public AggregatedHttpParser(SocketConnection connection) {
        this.connection = connection;
    }

    public synchronized HttpRequest parseRequest() {
        return requestParser.parse();
    }

    public synchronized HttpResponse parseResponse() {
        return responseParser.parse();
    }

    private HttpVersion parseHttpVersion(String string) {
        HttpVersion version = HttpVersion.getVersion(string);

        if (version == null) {
            throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Unknown HTTP version"));
        }

        return version;
    }

    private HttpHeaders parseHttpHeaders(String[] strings) {
        HttpHeaders headers = new HttpHeaders();

        for (int i = 1; i < strings.length; i++) {
            String[] split = strings[i].split(": ", 2);

            if (split.length == 2) {
                headers.add(split[0], split[1]);
            }
        }

        return headers;
    }

    private int parseContentLength(HttpHeaders headers) {
        int contentLength = 0;

        if (headers.containsKey("Content-Length")) {
            try {
                contentLength = Integer.parseInt(headers.getFirst("Content-Length"));

                if (contentLength < 1) {
                    throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid content length"));
                }
            } catch (NumberFormatException e) {
                throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid content length"));
            }
        }

        return contentLength;
    }

    private byte[] parseBody(VirtualByteBuffer inboundBuffer, int contentLength) {
        if (contentLength > 0) {
            if (inboundBuffer.available() >= contentLength) {
                return inboundBuffer.consume(contentLength);
            } else {
                return null;
            }
        }

        return new byte[0];
    }

    private class RequestParser {
        private String requestMethod = null;
        private String requestPath = null;
        private HttpVersion version = null;
        private HttpHeaders headers = null;
        private int contentLength = -1;

        public HttpRequest parse() {
            VirtualByteBuffer inboundBuffer = connection.inboundBuffer;

            synchronized (inboundBuffer) {
                if (contentLength == -1) {
                    int headerTerminator = inboundBuffer.scan("\r\n\r\n");

                    if (headerTerminator == -1) {
                        return null;
                    }

                    byte[] headerBytes = inboundBuffer.consume(headerTerminator);
                    String[] headerString = new String(headerBytes, StandardCharsets.UTF_8).split("\\r\\n");

                    if (headerString.length < 1) {
                        throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid headers"));
                    }

                    String[] requestLine = headerString[0].split(" ", 3);

                    if (requestLine.length < 3) {
                        throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid request line"));
                    }

                    requestMethod = requestLine[0];
                    requestPath = requestLine[1];
                    version = parseHttpVersion(requestLine[2]);

                    headers = parseHttpHeaders(headerString);
                    contentLength = parseContentLength(headers);

                    inboundBuffer.remove(4);
                }

                HttpRequest request = new HttpRequest(
                        requestMethod,
                        requestPath,
                        version,
                        headers,
                        parseBody(inboundBuffer, contentLength)
                );

                requestMethod = null;
                requestPath = null;
                version = null;
                headers = null;
                contentLength = -1;
                return request;
            }
        }
    }

    private class ResponseParser {
        private HttpVersion version = null;
        private int statusCode = -1;
        private String reasonPhrase = null;
        private HttpHeaders headers = null;
        private int contentLength = -1;

        public HttpResponse parse() {
            VirtualByteBuffer inboundBuffer = connection.inboundBuffer;

            synchronized (inboundBuffer) {
                if (contentLength == -1) {
                    int headerTerminator = inboundBuffer.scan("\r\n\r\n");

                    if (headerTerminator == -1) {
                        return null;
                    }

                    byte[] headerBytes = inboundBuffer.consume(headerTerminator);
                    String[] headerString = new String(headerBytes, StandardCharsets.UTF_8).split("\\r\\n");

                    if (headerString.length < 1) {
                        throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid headers"));
                    }

                    String[] responseLine = headerString[0].split(" ", 3);

                    if (responseLine.length < 3) {
                        throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid response line"));
                    }

                    version = parseHttpVersion(responseLine[0]);
                    reasonPhrase = responseLine[2];

                    try {
                        statusCode = Integer.parseInt(responseLine[1]);

                        if (statusCode < 100 || statusCode > 599) {
                            throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid status code"));
                        }
                    } catch (NumberFormatException e) {
                        throw new HttpException(HttpResponse.wrap(StatusCode.BAD_REQUEST, "Invalid status code"));
                    }

                    headers = parseHttpHeaders(headerString);
                    contentLength = parseContentLength(headers);

                    inboundBuffer.remove(4);
                }

                HttpResponse response = new HttpResponse(
                        version,
                        statusCode,
                        reasonPhrase,
                        headers,
                        parseBody(inboundBuffer, contentLength)
                );

                version = null;
                statusCode = -1;
                reasonPhrase = null;
                headers = null;
                contentLength = -1;
                return response;
            }
        }
    }
}
