package nl.radiantrealm.library.cache;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Cache<K, V> {
    protected final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    protected final LinkedHashMap<K, CacheEntry<V>> data;
    protected final CacheConfiguration configuration;

    public Cache(CacheConfiguration configuration) {
        this(configuration, 15, 0.75F);
    }
    
    public Cache(CacheConfiguration configuration, int initialCapacity, float loadFactor) {
        this.data = new LinkedHashMap<>(initialCapacity, loadFactor, configuration.accessOrder());
        this.configuration = configuration;

        executorService.scheduleWithFixedDelay(
                this::cleanUpCache,
                configuration.expirationIntervalMillis(),
                configuration.expirationIntervalMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    protected record CacheEntry<V>(
            V value,
            long creationMillis
    ) {}

    public void shutdown() {
        executorService.shutdown();
    }

    protected void cleanUpCache() {
        synchronized (data) {
            if (data.isEmpty()) {
                return;
            }

            long evictionMillis = System.currentTimeMillis() - configuration.cacheExpirationMillis();
            data.entrySet().removeIf(entry -> entry.getValue().creationMillis < evictionMillis);
        }
    }

    protected int ensureCacheCapacity(int addition) {
        synchronized (data) {
            int removal = data.size() + addition - configuration.maxCacheEntries();

            if (removal < 1) {
                return addition;
            } else if (removal >= configuration.maxCacheEntries()) {
                data.clear();
            } else {
                for (int i = 0; i < removal; i++) {
                    data.pollFirstEntry();
                }
            }

            return addition - removal;
        }
    }

    protected abstract V load(K key) throws Exception;

    protected Map<K, V> load(Set<K> keys) throws Exception {
        Map<K, V> map = new HashMap<>(keys.size());

        for (K key : keys) {
            map.put(key, load(key));
        }

        return map;
    }

    public V get(K key) throws Exception {
        if (key == null) {
            return null;
        }

        synchronized (data) {
            CacheEntry<V> entry = data.get(key);

            if (entry == null) {
                V value = load(key);
                put(key, value);
                return value;
            }

            return entry.value;
        }
    }

    public Map<K, V> get(Collection<K> keys) throws Exception {
        return get(new HashSet<>(keys));
    }

    public Map<K, V> get(Set<K> keys) throws Exception {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        synchronized (data) {
            Map<K, V> result = new HashMap<>(keys.size());
            Set<K> load = new HashSet<>();

            for (K key : keys) {
                CacheEntry<V> entry = data.get(key);

                if (entry == null) {
                    load.add(key);
                } else {
                    result.put(key, entry.value);
                }
            }

            if (!load.isEmpty()) {
                Map<K, V> loaded = load(load);
                put(loaded);
                result.putAll(loaded);
            }

            return result;
        }
    }

    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }

        synchronized (data) {
            ensureCacheCapacity(1);
            data.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
        }
    }

    public void put(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        synchronized (data) {
            int addition = ensureCacheCapacity(map.size());

            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (addition-- < 1) {
                    break;
                }

                if (entry.getKey() != null && entry.getValue() != null) {
                    data.put(entry.getKey(), new CacheEntry<>(entry.getValue(), System.currentTimeMillis()));
                }
            }
        }
    }

    public void remove(K key) {
        synchronized (data) {
            data.remove(key);
        }
    }

    public void remove(Collection<K> keys) {
        synchronized (data) {
            for (K key : keys) {
                data.remove(key);
            }
        }
    }

    public void clear() {
        synchronized (data) {
            data.clear();
        }
    }

    public int size() {
        synchronized (data) {
            return data.size();
        }
    }

    public boolean isEmpty() {
        synchronized (data) {
            return data.isEmpty();
        }
    }

    public Map<K, V> asMap() {
        synchronized (data) {
            Map<K, V> map = new HashMap<>(data.size());

            for (Map.Entry<K, CacheEntry<V>> entry : data.entrySet()) {
                map.put(entry.getKey(), entry.getValue().value);
            }

            return map;
        }
    }
}
