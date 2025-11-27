package nl.radiantrealm.library.net.http;

import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class AggregatedHttpRequest {
    private final HttpConnection connection;

    private String requestMethod = null;
    private URI requestURI = null;
    private HttpVersion version = null;
    private HttpHeaders headers = null;
    private int contentLength = -1;

    public AggregatedHttpRequest(HttpConnection connection) {
        this.connection = connection;
    }

    public synchronized HttpRequest parse() {
        VirtualByteBuffer inboundBuffer = connection.inboundBuffer;

        synchronized (inboundBuffer) {
            if (contentLength == -1) {
                int headerTerminator = inboundBuffer.scan("\r\n\r\n");

                if (headerTerminator == -1) {
                    return null;
                }

                byte[] headerBytes = inboundBuffer.poll(headerTerminator);
                String[] headerString = new String(headerBytes, StandardCharsets.UTF_8).split("\\r\\n");

                if (headerString.length < 1) {
                    throw new HttpException(HttpResponse.status(StatusCode.BAD_REQUEST));
                }

                String[] requestLine = headerString[0].split(" ", 3);

                if (requestLine.length < 3) {
                    throw new HttpException(HttpResponse.status(StatusCode.BAD_REQUEST, "Invalid request line"));
                }

                this.requestMethod = requestLine[0];
                this.version = HttpVersion.getVersion(requestLine[2]);

                if (version == null) {
                    throw new HttpException(HttpResponse.status(StatusCode.BAD_REQUEST, "Unknown HTTP version"));
                }

                try {
                    this.requestURI = URI.create(requestLine[1]);
                } catch (IllegalArgumentException | NullPointerException e) {
                    throw new HttpException(HttpResponse.status(StatusCode.BAD_REQUEST, "Invalid request URI"));
                }

                this.headers = new HttpHeaders();

                for (int i = 1; i < headerString.length; i++) {
                    String[] split = headerString[i].split(": ", 2);
                    headers.add(split[0], split[1]);
                }

                if (headers.containsKey("Content-Length")) {
                    try {
                        this.contentLength = Integer.parseInt(headers.getFirst("Content-Length"));
                    } catch (NumberFormatException e) {
                        throw new HttpException(HttpResponse.status(StatusCode.BAD_REQUEST));
                    }
                } else {
                    contentLength = 0;
                }

                inboundBuffer.poll(4);
            }

            if (contentLength == -1) {
                throw new HttpException(HttpResponse.status(StatusCode.UNPROCESSABLE_ENTITY));
            }

            byte[] body = new byte[0];
            if (contentLength > 0) {
                if (inboundBuffer.size() >= contentLength) {
                    body = inboundBuffer.poll(contentLength);
                } else {
                    return null;
                }
            }

            HttpRequest request = new HttpRequest(
                    requestMethod,
                    requestURI,
                    version,
                    headers,
                    body
            );

            requestMethod = null;
            requestURI = null;
            version = null;
            headers = null;
            contentLength = -1;
            return request;
        }
    }
}
