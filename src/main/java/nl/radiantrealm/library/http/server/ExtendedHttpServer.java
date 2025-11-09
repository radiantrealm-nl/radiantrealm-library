package nl.radiantrealm.library.http.server;

import com.sun.net.httpserver.HttpServer;
import nl.radiantrealm.library.http.HttpException;
import nl.radiantrealm.library.http.context.HttpExchangeContext;
import nl.radiantrealm.library.utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class ExtendedHttpServer {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final HttpServer server;

    public ExtendedHttpServer(InetSocketAddress socketAddress) throws IOException {
        this.server = createHttpServer(socketAddress);
    }

    protected HttpServer createHttpServer(InetSocketAddress socketAddress) throws IOException {
        return HttpServer.create(socketAddress, 0);
    }

    public void createContext(String path, ExtendedHttpHandler httpHandler) {
        server.createContext(path, exchange -> {
            try {
                HttpExchangeContext exchangeContext = new HttpExchangeContext(exchange);

                try {
                    httpHandler.handle(exchangeContext);
                } catch (HttpException e) {
                    exchangeContext.sendResponse(e.responseContext);
                }
            } catch (IOException e) {
                logger.error("Unexpecteed error while handling request.", e);
            }
        });
    }
}
