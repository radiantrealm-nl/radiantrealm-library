package nl.radiantrealm.library.processor;

import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class ProcessRouter implements ApplicationService {
    private final Logger logger = Logger.getLogger(this.getClass());

    private static final AtomicInteger processID = new AtomicInteger(0);
    private static final Map<Integer, Process<?>> processMap = new ConcurrentHashMap<>();

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
        task = executorService.scheduleWithFixedDelay(this::handleNextProcess, 0, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();
        task.cancel(false);
    }

    @SuppressWarnings("unchecked")
    protected <T extends DataObject<T>> void handleNextProcess() {
        if (processMap.isEmpty()) return;

        int nextProcessID = Collections.min(processMap.keySet());
        Process<T> process = (Process<T>) processMap.get(nextProcessID);

        try {
            ProcessResult result = process.handler().handle(process);
            process.consumer().accept(result);
        } catch (Exception e) {
            String error = String.format("Unexpected error in %s whilst processing request.", process.handler().getClass().getSimpleName());
            process.consumer().accept(ProcessResult.error(error, e));
            logger.error(error, e);
        }
    }

    public static <E extends ProcessType, T extends DataObject<T>> void createProcess(E processType, T data, Consumer<ProcessResult> consumer) throws IllegalArgumentException {
        if (processType == null || processType.getHandler() == null) throw new IllegalArgumentException("Process type cannot be null or empty.");
        if (data == null) throw new IllegalArgumentException("Input data for process cannot be null.");
        if (!processType.dto().isInstance(data)) throw new IllegalArgumentException("Invalid DataObject (DTO) class.");

        int nextProcessID = processID.incrementAndGet();

        processMap.put(nextProcessID, new Process<>(
                nextProcessID,
                processType.getHandler(),
                data,
                consumer
        ));
    }

    public static <E extends ProcessType, T extends DataObject<T>> void createProcess(E processType, T data) throws IllegalArgumentException {
        createProcess(processType, data, null);
    }
}
