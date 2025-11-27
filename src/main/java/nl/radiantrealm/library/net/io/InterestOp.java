package nl.radiantrealm.library.net.io;

import java.nio.channels.SelectionKey;

public enum InterestOp {
    OP_READ(SelectionKey.OP_READ),
    OP_WRITE(SelectionKey.OP_WRITE),
    OP_CONNECT(SelectionKey.OP_CONNECT),
    OP_ACCEPT(SelectionKey.OP_ACCEPT);

    public final int code;

    InterestOp(int code) {
        this.code = code;
    }
}
