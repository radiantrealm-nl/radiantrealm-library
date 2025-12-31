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

    public ProcessResult createProcess(ProcessHandler handler) throws Exception {
        if (handler == null) {
            return null;
        }

        CompletableFuture<ProcessResult> future = new CompletableFuture<>();

        Process process = new Process(
                handler,
                future
        );

        processQueue.add(process);
        return future.get();
    }

    private void processLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Process process = processQueue.take();

                try {
                    ProcessResult result = process.handler().handle(process);
                    process.future().complete(result);
                } catch (Exception e) {
                    process.future().completeExceptionally(e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
