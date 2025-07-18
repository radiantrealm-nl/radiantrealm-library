package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.Main;
import nl.radiantrealm.library.utils.Result;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CacheRegistry<K, V> {
    private final Map<K, V> dataMap = new ConcurrentHashMap<>();
    private final Map<K, Long> expiryMap = new ConcurrentHashMap<>();

    private final int expiry;

    /**
     * Constructor for the {@link CacheRegistry} class.
     * Initializes registry wiith a specified time and schdules a periodic cleanup task.
     *
     * @param expiry The expiry time in milliseconds for cached entries.
     * */
    public CacheRegistry(int expiry) {
        this.expiry = expiry;

        Main.scheduleAtFixedRate(0, 60000, () -> {
            long current = System.currentTimeMillis();

            for (Map.Entry<K, Long> entry : expiryMap.entrySet()) {
                if (current > entry.getValue()) {
                    dataMap.remove(entry.getKey());
                    expiryMap.remove(entry.getKey());
                }
            }
        });
    }

    /**
     * Loads a value associated with a given key. Subclasses must implement this method to define how the value for the specified key is retrieved or generated.
     *
     * @param key The key for which teh value needs to be loaded. Must not be null.
     * @return The value associated with the given key, or null if no vlaue is found.
     * @throws Exception If an error occurs during the loading process.
     * */
    protected abstract Result<V> load(K key) throws Exception;

    /**
     * Loads multiple values associated with their respective keys. Subclasses must implement this method to define how the values for the specified keys are retrieved or generated.
     *
     * @param keys A list of keys for which the values need to be loaded. Must not be null, empty, or contain only one key.
     * @return A map containing the keys and their associated values. Missing keys may be excluded from the map.
     * @throws Exception If an error occurs during the loading process.
     * @since 1.0.0
     * */
    protected abstract Map<K, Result<V>> load(List<K> keys) throws Exception;

    private void put(K key, V value) {
        dataMap.put(key, value);
        expiryMap.put(key, System.currentTimeMillis() + expiry);
    }

    /**
     * Retrieves an optional value associated with the given key from the cache.
     * If the key is not present in the cache, the value is loaded using the {@link #load(Object)} method.
     * If the value is successfully loaded, it is stored in the cache with an expiry time.
     *
     * @param key The key for which the value needs to be retrieved. Must not be null.
     * @return An {@link Result} containing the value if found or successfully loaded, or an empty {@link Result} if the key is null or the value could not be loaded.
     * @since 1.0.0
     * */
    public Result<V> get(K key) {
        if (key == null) return Result.error(new IllegalArgumentException("Invalid key."));

        if (dataMap.containsKey(key)) {
            return Result.ok(dataMap.get(key));
        }

        try {
            Result<V> value = load(key);

            if (value.isObjectEmpty()) {
                return value;
            }

            put(key, value.getObject());
            return value;
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    /**
     * Retrieves a key-value map based on the provided list of keys.
     * For each key in the list, the method attempts to retrieve the associated value from the cache.
     * If a key is not found in the cache, it is added to a list of keys to be loaded on demand.
     * The method then loads values for the missing keys using the {@link #load(List)} method and combines the results.
     *
     * @param keys A list of keys for which values need to be retrieved. Must not be null or empty.
     * @return A map containing the keys and their associated values. If a key is not found or cannot be loaded, it will not appear in the map.
     * @since 1.0.0
     *
     * @deprecated since v1.1.3 needs rework on the Result<V> record.
     * */
    @Deprecated
    public Map<K, Optional<V>> get(List<K> keys) {
        if (keys == null || keys.isEmpty()) return Map.of();

        Map<K, Optional<V>> result = new HashMap<>(keys.size());
        List<K> missingKeys = new ArrayList<>(keys.size());

        for (K key : keys) {
            if (dataMap.containsKey(key)) {
                result.put(key, Optional.of(dataMap.get(key)));
            } else {
                missingKeys.add(key);
            }
        }

//        try {
//            Map<K, Optional<V>> loadedMap = load(missingKeys);
//
//            for (Map.Entry<K, Optional<V>> entry : loadedMap.entrySet()) {
//                if (entry.getValue().isEmpty()) {
//                    result.put(entry.getKey(), Optional.empty());
//                } else {
//                    result.put(entry.getKey(), entry.getValue());
//                    put(entry.getKey(), entry.getValue().get());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Failed to load missing keys.", e);
//        }

        return result;
    }
}
