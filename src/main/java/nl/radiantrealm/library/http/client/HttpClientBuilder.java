package nl.radiantrealm.library.http.client;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class HttpClientBuilder {

    private HttpClientBuilder() {}

    public static HttpClient buildHttpClient(SSLContext context) throws Exception {
        return HttpClient.newBuilder()
                .sslContext(context)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public static HttpClient buildDefaultClient() throws Exception {
        return buildHttpClient(SSLContext.getDefault());
    }

    public static HttpClient buildInternalClient(String keystorePath, String keystorePassword) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");

        try (InputStream inputStream = Files.newInputStream(Path.of(keystorePath))) {
            trustStore.load(inputStream, keystorePassword.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), new SecureRandom());
        return buildHttpClient(context);
    }

    public static HttpClient buildInsecureClient() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {}
                    public void checkServerTrusted(X509Certificate[] xcs, String string) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        }, new SecureRandom());
        return buildHttpClient(context);
    }
}
