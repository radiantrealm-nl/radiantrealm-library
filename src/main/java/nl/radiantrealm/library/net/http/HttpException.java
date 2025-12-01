package nl.radiantrealm.library.net.http;

public class HttpException extends RuntimeException {
    public final HttpResponse response;

    public HttpException(HttpResponse response) {
        this.response = response;
    }

    public HttpException(Exception e, HttpResponse response) {
        super(e);

        this.response = response;
    }
}
