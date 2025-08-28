package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.ApplicationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public abstract class CacheRegistry<K, V> implements ApplicationService {
    private final Map<K, V> data = new ConcurrentHashMap<>();
    private final Map<K, Long> expiry = new ConcurrentHashMap<>();

    private final int expiryDuration;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> task;

    public CacheRegistry(int expiryDuration) {
        this.expiryDuration = expiryDuration;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        ApplicationService.super.start();
        task = executorService.scheduleAtFixedRate(this::cleanUpCache, expiryDuration, 60000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();
        task.cancel(false);
    }

    protected void cleanUpCache() {
        long timestamp = System.currentTimeMillis();

        for (Map.Entry<K, Long> entry : expiry.entrySet()) {
            if (timestamp > entry.getValue()) {
                remove(entry.getKey());
            }
        }
    }

    protected abstract V load(K key) throws Exception;

    protected abstract Map<K, V> load(List<K> keys) throws Exception;

    public V get(K key) throws Exception {
        if (key == null) throw new IllegalArgumentException("Key cannot be null or empty.");

        V value = data.get(key);

        if (value == null) {
            value = load(key);
        }

        return value;
    }

    public Map<K, V> get(List<K> keys) throws Exception {
        if (keys == null || keys.isEmpty()) throw new IllegalArgumentException("Keys cannot be null or empty.");

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

    public void put(K key, V value) throws IllegalArgumentException {
        if (key == null) throw new IllegalArgumentException("Key cannot be null or empty.");
        if (value == null) throw new IllegalArgumentException("Value cannot be null or empty.");

        data.put(key, value);
        expiry.put(key, System.currentTimeMillis() + expiryDuration);
    }

    public void remove(K key) {
        data.remove(key);
        expiry.remove(key);
    }
}
