package nl.radiantrealm.library;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends ApplicationController {

    public Main() {
        super(8779);
    }

    @Override
    protected void services() {
    }

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
    }

    public static void scheduleAtFixedRate(long initialDelay, long period, Runnable task) {
        executorService.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
