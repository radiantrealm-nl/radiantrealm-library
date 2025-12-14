package nl.radiantrealm.library.net.io;

import nl.radiantrealm.library.util.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SelectorEngine {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected final Queue<KeyAction> pendingKeyActions = new ConcurrentLinkedQueue<>();

    protected final ExecutorService executorService;
    protected final Selector selector;

    public SelectorEngine(SelectorConfiguration configuration) throws IOException {
        this.executorService = Executors.newFixedThreadPool(Math.max(2, configuration.threadPoolSize()));
        this.selector = Selector.open();
    }

    public void start() {
        isRunning.set(true);
        executorService.submit(this::IOLoop);
    }

    public void addKeyAction(KeyAction action) {
        pendingKeyActions.add(action);
    }

    public record KeyAction(
            SelectionKey key,
            InterestOp interestOp,
            boolean enable
    ) {}

    protected void IOLoop() {
        try {
            while (isRunning.get()) {
                try {
                    selector.select();
                    processKeyActions();

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isValid() && key.isReadable()) {
                            handleRead(key);
                        }

                        if (key.isValid() && key.isWritable()) {
                            handleWrite(key);
                        }

                        if (key.isValid() && key.isConnectable()) {
                            handleConnect(key);
                        }

                        if (key.isValid() && key.isAcceptable()) {
                            handleAccept(key);
                        }
                    }
                } catch (RuntimeException e) {
                    logger.warning("Uncaught Runtime exception", e);
                }
            }
        } catch (IOException e) {
            logger.error("Exception in main IO Loop", e);
        }
    }

    protected void processKeyActions() {
        while (!pendingKeyActions.isEmpty()) {
            KeyAction action = pendingKeyActions.poll();

            if (action == null || action.key() == null) {
                continue;
            }

            SelectionKey key = action.key();

            if (!key.isValid()) {
                continue;
            }

            try {
                if (action.enable()) {
                    key.interestOps(key.interestOps() | action.interestOp().code);
                } else {
                    key.interestOps(key.interestOps() & ~action.interestOp().code);
                }
            } catch (IllegalArgumentException e) {
                logger.warning("Failed to set interest op for key", e);
            }
        }
    }

    protected void handleRead(SelectionKey key) {}
    protected void handleWrite(SelectionKey key) {}
    protected void handleConnect(SelectionKey key) {}
    protected void handleAccept(SelectionKey key) {}
}
