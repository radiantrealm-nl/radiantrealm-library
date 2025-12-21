package nl.radiantrealm.library.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class Processor {
    private final BlockingQueue<Process> processQueue = new LinkedBlockingQueue<>();
    private final Thread processorThread;

    public Processor() {
        processorThread = new Thread(this::processLoop);
        processorThread.start();
    }

    public void shutdown() {
        processorThread.interrupt();
    }

    public ProcessResult createProcess(ProcessHandler handler) {
        if (handler == null) {
            return null;
        }

        CompletableFuture<ProcessResult> future = new CompletableFuture<>();

        Process process = new Process(
                handler,
                future
        );

        processQueue.add(process);

        try {
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    private void processLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Process process = processQueue.take();
                ProcessResult result = process.handler().handle(process);
                process.future().complete(result);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
