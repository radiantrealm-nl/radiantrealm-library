package nl.radiantrealm.library.net.http;

import java.net.InetSocketAddress;

public record HttpConfiguration(
        InetSocketAddress socketAddress,
        String pathPrefix,
        int threadPoolSize,
        int incomingBufferSize,
        int outgoingBufferSize
) {
    public static HttpConfiguration defaultConfiguration(InetSocketAddress socketAddress) {
        return new HttpConfiguration(
                socketAddress,
                "",
                16,
                4096,
                4096
        );
    }
}
