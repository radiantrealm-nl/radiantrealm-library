package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.http.enumerator.StatusCode;
import nl.radiantrealm.library.utils.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class AbstractQueueProcessor implements AutoCloseable {
    protected final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    protected final ConcurrentNavigableMap<Integer, Process> processMap = new ConcurrentSkipListMap<>();
    protected final AtomicInteger processID = new AtomicInteger(1);

    public AbstractQueueProcessor(long processDelay) {
        this.executorService.scheduleWithFixedDelay(
                this::handleNextProcess,
                processDelay,
                processDelay,
                TimeUnit.MILLISECONDS
        );
    }

    public AbstractQueueProcessor(Duration processDelay) {
        this(processDelay.toMillis());
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();

        if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }

    protected void handleNextProcess() {
        Map.Entry<Integer, Process> entry = processMap.pollFirstEntry();

        if (entry == null) {
            return;
        }

        Process process = entry.getValue();

        try {
            ProcessResult result = handleProcess(process);
            callback(process, result);
        } catch (ProcessException e) {
            callback(process, e.result);
        } catch (Exception e) {
            callback(process, ProcessResult.error(StatusCode.SERVER_ERROR));
        }
    }

    protected ProcessResult handleProcess(Process process) throws Exception {
        return process.handler().handle(process);
    }

    protected void callback(Process process, ProcessResult result) {
        if (process.consumer() != null) {
            process.consumer().accept(result);
        }
    }

    public int createProcess(@NotNull ProcessHandler handler, JsonObject object, Consumer<ProcessResult> consumer) {
        int nextProcessID = processID.incrementAndGet();

        processMap.put(nextProcessID, new Process(
                nextProcessID,
                Objects.requireNonNull(handler),
                object,
                consumer
        ));

        return nextProcessID;
    }

    public int createProcess(@NotNull ProcessHandler handler, JsonObject object) {
        return createProcess(handler, object, null);
    }

    public int createProcess(@NotNull ProcessHandler handler, Consumer<ProcessResult> consumer) {
        return createProcess(handler, null, consumer);
    }

    public int createProcess(@NotNull ProcessHandler handler) {
        return createProcess(handler, null, null);
    }
}
