package nl.radiantrealm.library.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public abstract class CacheRegistry<K, V> {
    protected final Map<K, V> data = new ConcurrentHashMap<>();
    protected final Map<K, Long> expiry = new ConcurrentHashMap<>();

    protected final Duration expiryDuration;
    protected final ScheduledExecutorService executorService;
    protected ScheduledFuture<?> task;

    public CacheRegistry(Duration expiryDuration) {
        this.expiryDuration = expiryDuration;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.task = buildCleanUpTask();
    }

    protected ScheduledFuture<?> buildCleanUpTask() {
        return executorService.scheduleAtFixedRate(this::cleanUpCache, expiryDuration.toSeconds(), 60, TimeUnit.SECONDS);
    }

    protected void cleanUpCache() {
        long timestamp = System.currentTimeMillis();

        for (Map.Entry<K, Long> entry : expiry.entrySet()) {
            if (timestamp > entry.getValue()) {
                remove(entry.getKey(), data.get(entry.getKey()));
            }
        }
    }

    protected abstract V load(K key) throws Exception;

    protected Map<K, V> load(List<K> keys) throws Exception {
        Map<K, V> map = new HashMap<>(keys.size());

        for (K key : keys) {
            map.put(key, load(key));
        }

        return map;
    }

    public V get(K key) throws Exception {
        V value = data.get(key);

        if (value == null) {
            value = load(key);
        }

        return value;
    }

    public Map<K, V> get(List<K> keys) throws Exception {
        if (keys.isEmpty()) return null;

        if (keys.size() == 1) {
            K key = keys.getFirst();
            V value = get(key);
            return Map.of(key, value);
        }

        Map<K, V> cachedKeys = new HashMap<>(keys.size());
        List<K> loadKeys = new ArrayList<>();

        for (K key : keys) {
            V value = data.get(key);

            if (value == null) {
                loadKeys.add(key);
            } else {
                cachedKeys.put(key, value);
            }
        }

        cachedKeys.putAll(load(loadKeys));

        for (K key : keys) {
            V value = cachedKeys.get(key);
            put(key, value);
        }

        return cachedKeys;
    }

    public void put(K key, V value) {
        data.put(key, value);
        expiry.put(key, System.currentTimeMillis() + expiryDuration.toMillis());
    }

    public void put(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void remove(K key) {
        data.remove(key);
        expiry.remove(key);
    }

    public void remove(K key, V value) {
        remove(key);
    }

    public void remove(List<K> list) {
        for (K key : list) {
            remove(key);
        }
    }

    public void remove(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            remove(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        data.clear();
        expiry.clear();
    }
}
