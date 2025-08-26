package nl.radiantrealm.library.processor;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.utils.Logger;
import nl.radiantrealm.library.utils.Result;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ProcessRouter implements ApplicationService {
    private final Logger logger = Logger.getLogger(this.getClass());

    private static final Map<Integer, ProcessRequest> procesMap = new ConcurrentHashMap<>();
    private static final AtomicInteger processID = new AtomicInteger(0);

    private final int delay;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> task;

    public ProcessRouter(int delay) {
        this.delay = delay;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        ApplicationService.super.start();
        task = executorService.scheduleAtFixedRate(this::handleNextProcess, 0, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();
        task.cancel(false);
    }

    protected void handleNextProcess() {
        Result<ProcessRequest> tryCatch = Result.tryCatch(() -> procesMap.get(Collections.min(procesMap.keySet())));

        if (tryCatch.isObjectEmpty()) {
            logger.error("Failed to fetch next process request.", tryCatch.getError());
            return;
        }

        ProcessRequest request = tryCatch.getObject();

        try {
            ProcessResult result = request.processType().getHandler().handle(request);
            request.callback().callback(result);
        } catch (Exception e) {
            logger.error(String.format("Unexpected exception in %s.", request.operationalClassName()), e);
            request.callback().callback(new ProcessResult(
                    request.processID(),
                    false,
                    null
            ));
        }

        procesMap.remove(request.processID());
    }

    public static synchronized void createProcess(ProcessType type, JsonObject object, ProcessResultCallback callback) {
        ProcessHandler handler = type.getHandler();

        if (handler == null) {
            throw new IllegalArgumentException("Process does not exist.");
        }

        int nextProcessID = processID.incrementAndGet();
        procesMap.put(nextProcessID, new ProcessRequest(
                nextProcessID,
                type,
                object,
                callback
        ));
    }
}
