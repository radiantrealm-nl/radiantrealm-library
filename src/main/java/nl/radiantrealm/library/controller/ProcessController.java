package nl.radiantrealm.library.controller;

import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessRequest;
import nl.radiantrealm.library.processor.ProcessResultListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ProcessController {
    protected static final Map<Integer, ProcessRequest<?>> processMap = new ConcurrentHashMap<>();
    protected static final Map<Integer, ProcessResultListener> listenerMap = new ConcurrentHashMap<>();

    protected static final ReentrantLock lock = new ReentrantLock();

    protected static final AtomicInteger processID = new AtomicInteger(0);
    protected static final AtomicBoolean allowIncoming = new AtomicBoolean(false);
    protected static final AtomicReference<ProcessorState> processorState = new AtomicReference<>(ProcessorState.STOPPED);

    public ProcessController(int processInterval) {
    }

    public enum ProcessorState {
        IDLE,
        PROCESSING,
        STOPPED,
    }

    protected void start() {
        if (!processorState.get().equals(ProcessorState.PROCESSING)) {
            processorState.set(ProcessorState.IDLE);
        }
    }

    protected void stop() {
        processorState.set(ProcessorState.STOPPED);
    }

    protected void setAllowIncoming(boolean allowIncoming) {
        ProcessController.allowIncoming.set(allowIncoming);
    }

    public static <T> boolean createProcess(ProcessHandler<T> handler, ProcessResultListener listener) {
        if (!allowIncoming.get()) {
            return false;
        }

        int nextProcessID = processID.incrementAndGet();

        if (listener != null) {
            listenerMap.put(nextProcessID, listener);
        }

        processMap.put(nextProcessID, new ProcessRequest<>(
                nextProcessID,
                handler
        ));

        return true;
    }
}
