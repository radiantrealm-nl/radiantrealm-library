package nl.radiantrealm.library;

import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationController {
    private final int servicePort;
    protected final List<Class<? extends ApplicationService>> services = new ArrayList<>();

    public ApplicationController(int port) {
        this.servicePort = port;
    }

    protected abstract void services();

    protected void registerService(Class<? extends ApplicationService> service) {
        services.add(service);
    }
}
