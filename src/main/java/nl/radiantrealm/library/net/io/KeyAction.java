package nl.radiantrealm.library.net.io;

import java.nio.channels.SelectionKey;

public record KeyAction(
        SelectionKey key,
        InterestOp interestOp,
        boolean enable
) {}
