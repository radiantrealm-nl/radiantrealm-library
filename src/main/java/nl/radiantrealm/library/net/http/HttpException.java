package nl.radiantrealm.library.net.http;

import java.util.Objects;

public class HttpException extends RuntimeException {
    public final HttpResponse response;

    public HttpException(HttpResponse response) {
        this.response = Objects.requireNonNull(response);
    }

    public HttpException(RuntimeException e, HttpResponse response) {
        super(e);
        this.response = Objects.requireNonNull(response);
    }
}
