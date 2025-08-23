package nl.radiantrealm.library.cache;

import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.utils.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public abstract class CacheRegistry<K, V> implements ApplicationService {
    private final Map<K, V> data = new ConcurrentHashMap<>();
    private final Map<K, Long> expiry = new ConcurrentHashMap<>();

    private ScheduledFuture<?> scheduledFuture;
    private final int duration;

    public CacheRegistry(int duration) {
        this.duration = duration;
    }

    //Start/stop system to be re-implemented in next branch.

    protected abstract Result<V> load(K key);

    private void cleanUpCache() {
        long timestamp = System.currentTimeMillis();

        for (Map.Entry<K, Long> entry : expiry.entrySet()) {
            if (timestamp > entry.getValue()) {
                remove(entry.getKey());
            }
        }
    }

    public Result<V> get(K key) {
        if (key == null) {
            return Result.error(new IllegalArgumentException("Key can not be null."));
        }

        if (data.containsKey(key)) {
            return Result.ok(data.get(key));
        }

        Result<V> result = load(key);

        if (!result.isObjectEmpty()) {
            put(key, result.getObject());
        }

        return result;
    }

    public void put(K key, V value) {
        if (key == null || value == null) return;

        data.put(key, value);
        expiry.put(key, System.currentTimeMillis() + duration);
    }

    public void remove(K key) {
        data.remove(key);
        expiry.remove(key);
    }
}
