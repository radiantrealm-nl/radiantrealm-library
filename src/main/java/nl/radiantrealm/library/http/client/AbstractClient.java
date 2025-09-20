package nl.radiantrealm.library.http.client;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.http.HttpMethod;
import nl.radiantrealm.library.http.MimeType;
import nl.radiantrealm.library.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class AbstractClient implements ApplicationService {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final HttpClient client;

    public AbstractClient(HttpClient client) {
        this.client = client;
    }

    protected Duration requestTimeoutSeconds() {
        return Duration.ofMinutes(5);
    }

    public HttpResponse<String> sendRequest(String url, String method, String mimeType, String body) throws Exception {
        if (url == null) throw new IllegalArgumentException("Request URI may not be empty.");
        if (method == null) method = "GET";
        if (mimeType == null) mimeType = "application/json";
        if (body == null) body = "{}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(requestTimeoutSeconds())
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", mimeType)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
    }

    public HttpResponse<String> sendRequest(String url, HttpMethod method, JsonObject object) throws Exception {
        return sendRequest(url, method.method, MimeType.JSON.type, object.getAsString());
    }
}
