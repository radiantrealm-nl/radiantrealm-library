package nl.radiantrealm.library.net.ws;

import nl.radiantrealm.library.net.http.HttpConnection;
import nl.radiantrealm.library.net.http.HttpExchange;
import nl.radiantrealm.library.net.http.HttpHandler;
import nl.radiantrealm.library.net.http.HttpResponse;
import nl.radiantrealm.library.net.io.InterestOp;
import nl.radiantrealm.library.net.io.SelectorEngine;
import nl.radiantrealm.library.util.VirtualByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class WebsocketEngine extends SelectorEngine implements HttpHandler {
    protected final Queue<HttpConnection> pendingUpgrades = new ConcurrentLinkedQueue<>();

    protected final WebsocketConfiguration configuration;

    public WebsocketEngine(WebsocketConfiguration configuration) throws IOException {
        super(configuration.threadPoolSize());

        this.configuration = configuration;
    }

    @Override
    public void handle(HttpExchange exchange) {
        HttpResponse response = WebsocketHandshake.perform(exchange.request());

        HttpConnection connection = exchange.connection();
        connection.key.cancel();

        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        VirtualByteBuffer outboundBuffer = connection.outboundBuffer;

        synchronized (outboundBuffer) {
            outboundBuffer.add(buffer);
        }

        pendingUpgrades.add(connection);
        selector.wakeup();
    }

    @Override
    protected void processKeyActions() {
        while (!pendingUpgrades.isEmpty()) {
            HttpConnection connection = pendingUpgrades.poll();

            try {
                SelectionKey key = connection.channel.register(selector, SelectionKey.OP_READ);
                WebsocketSession session = WebsocketSession.upgrade(
                        connection,
                        this,
                        key
                );

                key.attach(session);
                session.enableInterestOp(InterestOp.OP_WRITE);

                executorService.submit(() -> {
                    try {
                        upgradeSession(session);
                    } catch (WebsocketException e) {
                        sendClosingFrame(session, e.frame);
                    } catch (RuntimeException e) {
                        sendClosingFrame(session, WebsocketExitCode.INTERNAL_SERVER_ERROR.generateFrame());
                    }
                });
            } catch (IOException e) {
                logger.error("Exception while upgrading request", e);
            }
        }

        super.processKeyActions();
    }

    @Override
    protected void handleRead(SelectionKey key) {
        if (key.attachment() instanceof WebsocketSession session) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            try {
                int read = session.channel.read(buffer);

                if (read == -1) {
                    session.close(true);
                    closeSession(session);
                    return;
                }

                if (read == 0) {
                    return;
                }
            } catch (IOException e) {
                logger.error("Exception while reading channel", e);
                sendClosingFrame(session, WebsocketExitCode.INTERNAL_SERVER_ERROR.generateFrame());
                return;
            }

            session.addInboundBuffer(buffer.flip());

            executorService.submit(() -> {
                try {
                    WebsocketFrame frame = session.aggregatedWebsocketFrame.parse();

                    if (frame == null) {
                        return;
                    }

                    onFrame(session, frame);
                } catch (WebsocketException e) {
                    sendClosingFrame(session, e.frame);
                } catch (RuntimeException e) {
                    sendClosingFrame(session, WebsocketExitCode.INTERNAL_SERVER_ERROR.generateFrame());
                }
            });
        }
    }

    @Override
    protected void handleWrite(SelectionKey key) {
        if (key.attachment() instanceof WebsocketSession session) {
            VirtualByteBuffer outboundBuffer = session.outboundBuffer;

            try {
                synchronized (outboundBuffer) {
                    while (!outboundBuffer.isEmpty()) {
                        ByteBuffer buffer = ByteBuffer.wrap(outboundBuffer.peek(1024));
                        int written = session.channel.write(buffer);
                        outboundBuffer.poll(written);

                        if (buffer.hasRemaining()) {
                            return;
                        }
                    }

                    if (outboundBuffer.isEmpty()) {
                        session.disableInterestOp(InterestOp.OP_WRITE);
                        session.wakeup();

                        if (session.awaitClosing.get()) {
                            session.close(true);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error while writing channel", e);
                sendClosingFrame(session, WebsocketExitCode.INTERNAL_SERVER_ERROR.generateFrame());
            } catch (RuntimeException e) {
                sendClosingFrame(session, WebsocketExitCode.INTERNAL_SERVER_ERROR.generateFrame());
            }
        }
    }

    protected void upgradeSession(WebsocketSession session) {}
    protected void onFrame(WebsocketSession session, WebsocketFrame frame) {}
    protected void closeSession(WebsocketSession session) {}

    protected void sendClosingFrame(WebsocketSession session, WebsocketFrame frame) {
        try (session) {
            ByteBuffer buffer = ByteBuffer.wrap(frame.getBytes());
            VirtualByteBuffer outboundBuffer = session.outboundBuffer;

            synchronized (outboundBuffer) {
                outboundBuffer.add(buffer);
            }

            session.enableInterestOp(InterestOp.OP_WRITE);
            session.wakeup();
        }

        closeSession(session);
    }
}
