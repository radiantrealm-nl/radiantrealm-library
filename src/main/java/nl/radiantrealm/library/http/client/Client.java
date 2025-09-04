package nl.radiantrealm.library.http.client;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.http.HttpMethod;
import nl.radiantrealm.library.http.MimeType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

public abstract class Client {
    protected final HttpClient client;

    public Client() throws Exception {
        this.client = buildHttpClient(CertificateLevel.PUBLIC_CA);
    }

    public Client(CertificateLevel level) throws Exception {
        this.client = buildHttpClient(level);
    }

    protected HttpClient buildHttpClient(CertificateLevel level) throws Exception {
        SSLContext context = switch (level) {
            case INTERNAL_CA -> createInternalSSLContext();
            case INSECURE_CA -> createInsecureSSLContext();

            default -> createDefaultSSLContext();
        };

        return HttpClient.newBuilder()
                .sslContext(context)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    protected String internalJKSPath() {
        return "internal.jks";
    }

    protected String internalJKSPassword() {
        return "changeit";
    }

    protected Duration requestTimeoutSeconds() {
        return Duration.ofMinutes(5);
    }

    protected SSLContext createDefaultSSLContext() throws Exception {
        return SSLContext.getDefault();
    }

    protected SSLContext createInternalSSLContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");

        try (InputStream inputStream = Files.newInputStream(Path.of(internalJKSPath()))) {
            trustStore.load(inputStream, internalJKSPassword().toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), new SecureRandom());
        return context;
    }

    protected SSLContext createInsecureSSLContext() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {}
                    public void checkServerTrusted(X509Certificate[] xcs, String string) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        }, new SecureRandom());
        return context;
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
