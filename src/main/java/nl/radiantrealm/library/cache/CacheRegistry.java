package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.Main;
import nl.radiantrealm.library.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CacheRegistry<K, V> {
    private final Logger logger = Logger.getLogger(CacheRegistry.class);

    private final Map<K, V> dataMap = new ConcurrentHashMap<>();
    private final Map<K, Long> expiryMap = new ConcurrentHashMap<>();

    private final int expiry;

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

    protected abstract V load(K key);

    protected abstract Map<K, V> load(List<K> keys);

    public record Index<V>(Class<?> type) {}

    private void put(K key, V value) {
        if (key != null || value != null) {
            dataMap.put(key, value);
            expiryMap.put(key, System.currentTimeMillis() + expiry);
        }
    }

    public V get(K key) {
        if (key == null) return null;

        return dataMap.computeIfAbsent(key, k -> {
            V value = load(key);
            put(key, value);
            return value;
        });
    }

    public Map<K, V> get(List<K> keys) {
        if (keys == null) return null;
        if (keys.isEmpty()) return null;
        if (keys.size() == 1) return Map.of(keys.getFirst(), get(keys.getFirst()));

        Map<K, V> result = new HashMap<>();
        List<K> notFoundYet = new ArrayList<>();

        for (K key : keys) {
            if (dataMap.containsKey(key)) {
                result.put(key, dataMap.get(key));
            } else {
                notFoundYet.add(key);
            }
        }

        if (!notFoundYet.isEmpty()) {
            Map<K, V> onDemandResult = load(notFoundYet);
            result.putAll(onDemandResult);
        }

        return result;
    }
}
