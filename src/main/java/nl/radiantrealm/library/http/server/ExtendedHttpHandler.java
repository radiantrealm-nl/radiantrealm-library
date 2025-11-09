package nl.radiantrealm.library.http.server;

import nl.radiantrealm.library.http.context.HttpExchangeContext;

import java.io.IOException;

public interface ExtendedHttpHandler {
    void handle(HttpExchangeContext exchangeContext) throws IOException;
}
