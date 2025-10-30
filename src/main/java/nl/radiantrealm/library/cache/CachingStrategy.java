package nl.radiantrealm.library.cache;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

public record CachingStrategy(
        long evictionDuration,
        long evictionInterval,
        int maxCacheEntries
) {
    public CachingStrategy(@NotNull Duration evictionDuration, @NotNull Duration evictionInterval, int maxCacheEntries) {
        this(
                Objects.requireNonNull(evictionDuration).toMillis(),
                Objects.requireNonNull(evictionInterval).toMillis(),
                maxCacheEntries
        );
    }
}
