package nl.radiantrealm.library.cache;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

public abstract class AbstractCache<K, V> {
    protected final ScheduledExecutorService executorService;
    protected final ConcurrentHashMap<K, CacheEntry<V>> data = new ConcurrentHashMap<>();
    protected final ConcurrentLinkedQueue<K> linkedQueue = new ConcurrentLinkedQueue<>();
    protected final CachingStrategy strategy;
    protected final long evictionDuration;
    protected final long evictionInterval;

    public AbstractCache(@NotNull CachingStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
        this.evictionDuration = Objects.requireNonNull(strategy.evictionDuration()).toMillis();
        this.evictionInterval = Objects.requireNonNull(strategy.evictionInterval()).toMillis();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(
                this::evictEntries,
                evictionInterval,
                evictionInterval,
                TimeUnit.MILLISECONDS
        );
    }

    protected record CacheEntry<V>(V value, long evictionTime) {}

    protected void evictEntries() {
        long timestamp = System.currentTimeMillis();

        for (Map.Entry<K, CacheEntry<V>> entry : data.entrySet()) {
            if (timestamp > entry.getValue().evictionTime) {
                remove(entry.getKey());
            }
        }
    }

    protected abstract V load(@NotNull K key) throws Exception;

    protected HashMap<K, V> load(ArrayList<@NotNull K> keys) throws Exception {
        HashMap<K, V> map = new HashMap<>(keys.size());

        for (K key : keys) {
            map.put(key, load(key));
        }

        return map;
    }

    public V get(@NotNull K key) throws Exception {
        CacheEntry<V> entry = data.getOrDefault(key, null);

        if (entry == null) {
            V value = load(key);
            put(key, value);
            return value;
        }

        return entry.value;
    }

    public HashMap<K, V> get(ArrayList<@NotNull K> keys) throws Exception {
        if (Objects.requireNonNull(keys).isEmpty()) return null;

        HashMap<K, V> cachedKeys = new HashMap<>(keys.size());

        if (keys.size() == 1) {
            K key = keys.getFirst();
            cachedKeys.put(key, get(key));
            return cachedKeys;
        }

        ArrayList<K> loadKeys = new ArrayList<>();

        for (K key : keys) {
            CacheEntry<V> entry = data.getOrDefault(key, null);

            if (entry == null) {
                loadKeys.add(key);
            } else {
                cachedKeys.put(key, entry.value);
            }
        }

        cachedKeys.putAll(load(loadKeys));
        put(cachedKeys);
        return cachedKeys;
    }

    public void put(@NotNull K key, @NotNull V value) {
        data.put(key, new CacheEntry<>(
                Objects.requireNonNull(value),
                System.currentTimeMillis() + evictionDuration
        ));

        if (data.size() > strategy.maxCacheEntries()) {
            K oldestKey = linkedQueue.poll();

            if (oldestKey != null) {
                data.remove(oldestKey);
            }
        }
    }

    public void put(Map<@NotNull K, @NotNull V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            data.put(entry.getKey(), new CacheEntry<>(
                    Objects.requireNonNull(entry.getValue()),
                    System.currentTimeMillis() + evictionDuration
            ));
        }

        while (data.size() > strategy.maxCacheEntries()) {
            K oldestKey = linkedQueue.poll();

            if (oldestKey != null) {
                data.remove(oldestKey);
            } else {
                break;
            }
        }
    }

    public void remove(@NotNull K key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
        linkedQueue.clear();
    }

    public boolean contains(K key) {
        return data.containsKey(key);
    }

    public int size() {
        return data.size();
    }

    public HashMap<K, V> asMap() {
        HashMap<K, V> map = new HashMap<>(data.size());

        for (Map.Entry<K, CacheEntry<V>> entry : data.entrySet()) {
            map.put(entry.getKey(), entry.getValue().value);
        }

        return map;
    }
}
