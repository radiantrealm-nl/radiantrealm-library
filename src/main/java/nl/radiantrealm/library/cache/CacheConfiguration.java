package nl.radiantrealm.library.cache;

public record CacheConfiguration(
        boolean accessOrder,
        int maxCacheEntries,
        long cacheExpirationMillis,
        long expirationIntervalMillis
) {
    public static final CacheConfiguration defaultConfiguration = new CacheConfiguration(
            true,
            32,
            900_000,
            15_000
    );
}
