package nl.radiantrealm.library.cache;

import java.time.Duration;

public record CachingStrategy(
        Duration evictionDuration,
        Duration evictionInterval,
        int maxCacheEntries
) {}
