package nl.radiantrealm.library.http.client;

import nl.radiantrealm.library.http.enumerator.HttpMethod;
import nl.radiantrealm.library.http.enumerator.MediaType;
import nl.radiantrealm.library.utils.Logger;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class AbstractHttpClient {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final HttpClient client;

    public AbstractHttpClient(HttpClient client) {
        this.client = client;
    }

    protected Duration requestTimeoutDuration() {
        return Duration.ofMinutes(1);
    }

    public HttpResponse<String> sendRequest(String url, String method, String mediaType, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(requestTimeoutDuration())
                .method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body))
                .header("Content-Type", mediaType)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
    }

    public HttpResponse<String> sendRequest(String url, HttpMethod method, MediaType mediaType, String body) throws Exception {
        return sendRequest(url, method.name(), mediaType.type, body);
    }

    public HttpResponse<String> sendRequest(String url, HttpMethod method, JsonObject object) throws Exception {
        return sendRequest(url, method.name(), MediaType.JSON.type, object.toString());
    }
}
