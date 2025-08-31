package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.utils.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class ProcessRouter implements ApplicationService {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected static final AtomicInteger processID = new AtomicInteger(0);
    protected static final Map<String, ProcessHandler> handlerMap = new ConcurrentHashMap<>();
    protected static final Map<Integer, Process> processMap = new ConcurrentHashMap<>();

    protected final int delay;
    protected final ScheduledExecutorService executorService;
    protected ScheduledFuture<?> task;

    public ProcessRouter(int delay) {
        this.delay = delay;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        ApplicationService.super.start();
        task = executorService.scheduleWithFixedDelay(this::handleNextProcess, 0, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();
        task.cancel(false);
    }

    protected void handleNextProcess() {
        if (processMap.isEmpty()) return;

        int nextProcessID = Collections.min(processMap.keySet());
        Process process = processMap.remove(nextProcessID);

        if (process == null) return;

        try {
            ProcessResult result = process.handler().handle(process);
            process.consumer().accept(result);
        } catch (Exception e) {
            String error = String.format("Unexpected error in %s whilst processing request.", process.handler().getClass().getSimpleName());
            process.consumer().accept(ProcessResult.error(500, error, e));
            logger.error(error, e);
        }
    }

    protected void registerHandler(String path, ProcessHandler handler) throws IllegalArgumentException {
        if (path == null) throw new IllegalArgumentException("Handler path cannot be null.");
        if (handler == null) throw new IllegalArgumentException("Handler cannot be null or empty.");

        handlerMap.put(path, handler);
    }

    public static void createProcess(String path, JsonObject object, Consumer<ProcessResult> consumer) throws IllegalArgumentException {
        ProcessHandler handler = handlerMap.get(path);

        if (handler == null) throw new IllegalArgumentException("Handler not found.");

        int nextProcessID = processID.incrementAndGet();

        processMap.put(nextProcessID, new Process(
                nextProcessID,
                handler,
                object,
                consumer
        ));
    }
}
