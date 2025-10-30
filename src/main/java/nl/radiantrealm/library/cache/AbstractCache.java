package nl.radiantrealm.library.cache;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

public abstract class AbstractCache<K, V> implements AutoCloseable {
    protected final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    protected final ConcurrentHashMap<K, CacheEntry<V>> data = new ConcurrentHashMap<>();
    protected final ConcurrentLinkedQueue<K> linkedQueue = new ConcurrentLinkedQueue<>();
    protected final CachingStrategy strategy;

    public AbstractCache(@NotNull CachingStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
        this.executorService.scheduleAtFixedRate(
                this::evictEntries,
                strategy.evictionInterval(),
                strategy.evictionInterval(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();

        if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }

    protected record CacheEntry<V>(V value, long evictionTime) {}

    protected void evictEntries() {
        long timestamp = System.currentTimeMillis();

        data.forEach((key, value) -> {
            if (timestamp > value.evictionTime) {
                remove(key, value.value);
            }
        });
    }

    protected void evictIfFull() {
        while (data.size() > strategy.maxCacheEntries()) {
            K oldestKey = linkedQueue.poll();

            if (oldestKey != null) {
                data.remove(oldestKey);
            } else {
                break;
            }
        }
    }

    protected abstract V load(@NotNull K key) throws Exception;

    protected Map<K, V> load(@NotNull Set<K> keys) throws Exception {
        return load(new ArrayList<>(keys));
    }

    protected Map<K, V> load(@NotNull List<K> keys) throws Exception {
        Map<K, V> map = new HashMap<>(keys.size());

        for (K key : keys) {
            map.put(key, load(key));
        }

        return map;
    }

    public V get(@NotNull K key) throws Exception {
        CacheEntry<V> entry = data.get(key);

        if (entry == null) {
            V value = load(key);
            put(key, value);
            return value;
        }

        return entry.value;
    }

    public Map<K, V> get(@NotNull Set<K> keys) throws Exception {
        return get(new ArrayList<>(keys));
    }

    public Map<K, V> get(@NotNull List<K> keys) throws Exception {
        if (Objects.requireNonNull(keys).isEmpty()) {
            return Map.of();
        }

        Map<K, V> cached = new HashMap<>(keys.size());

        if (keys.size() == 1) {
            K key = keys.getFirst();
            cached.put(key, get(key));
            return cached;
        }

        List<K> fetch = new ArrayList<>();

        for (K key : keys) {
            CacheEntry<V> entry = data.get(key);

            if (entry == null) {
                fetch.add(key);
            } else {
                cached.put(key, entry.value);
            }
        }

        Map<K, V> loaded = load(fetch);
        put(loaded);
        cached.putAll(loaded);
        return cached;
    }

    public void put(@NotNull K key, @NotNull V value) {
        linkedQueue.add(key);
        data.put(key, new CacheEntry<>(
                Objects.requireNonNull(value),
                System.currentTimeMillis() + strategy.evictionDuration()
        ));

        evictIfFull();
    }

    public void put(@NotNull Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            linkedQueue.add(entry.getKey());
            data.put(entry.getKey(), new CacheEntry<>(
                    Objects.requireNonNull(entry.getValue()),
                    System.currentTimeMillis() + strategy.evictionDuration()
            ));
        }

        evictIfFull();
    }

    public void remove(@NotNull K key) {
        data.remove(key);
        linkedQueue.remove(key);
    }

    public void remove(@NotNull K key, @NotNull V value) {
        data.remove(key);
        linkedQueue.remove(key);
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

    public Map<K, V> asMap() {
        Map<K, V> map = new HashMap<>(data.size());

        for (Map.Entry<K, CacheEntry<V>> entry : data.entrySet()) {
            map.put(entry.getKey(), entry.getValue().value);
        }

        return map;
    }
}
