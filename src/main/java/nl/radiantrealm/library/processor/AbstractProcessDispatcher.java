package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.Logger;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class AbstractProcessDispatcher {
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected static final AtomicInteger processID = new AtomicInteger(0);
    protected static final Map<Integer, Process> processMap = new ConcurrentHashMap<>();
    protected final ScheduledExecutorService executorService;
    protected ScheduledFuture<?> task;

    public AbstractProcessDispatcher() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        task = buildHandleNextProcessTask();
    }

    protected ScheduledFuture<?> buildHandleNextProcessTask() {
        return executorService.scheduleWithFixedDelay(this::handleNextProcess, 0, processInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    protected Duration processInterval() {
        return Duration.ofMillis(100);
    }

    protected void handleNextProcess() {
        if (processMap.isEmpty()) return;

        int nextProcessID = Collections.min(processMap.keySet());
        Process process = processMap.remove(nextProcessID);

        try {
            process.callback(process.handle());
        } catch (ProcessException e) {
            process.callback(e.result);
        } catch (Exception e) {
            logger.error(String.format("Unexpected error in '%s' whilst processing request.", process.handler().getClass().getSimpleName()), e);
            process.callback(ProcessResult.error(StatusCode.SERVER_ERROR, "Server error."));
        }
    }

    public static void createProcess(ProcessHandler handler, JsonObject object, Consumer<ProcessResult> consumer) throws IllegalArgumentException {
        int nextProcessID = processID.incrementAndGet();

        processMap.put(nextProcessID, new Process(
                nextProcessID,
                handler,
                object,
                consumer
        ));
    }

    public static void createProcess(ProcessHandler handler, Consumer<ProcessResult> consumer) {
        createProcess(handler, null, consumer);
    }

    public static void createProcess(ProcessHandler handler, JsonObject object) {
        createProcess(handler, object, null);
    }

    public static void createProcess(ProcessHandler handler) {
        createProcess(handler, null, null);
    }
}
