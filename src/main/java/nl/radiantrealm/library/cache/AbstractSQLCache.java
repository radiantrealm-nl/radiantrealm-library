package nl.radiantrealm.library.cache;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractSQLCache<K, V> extends AbstractCache<K, V> {

    public AbstractSQLCache(@NotNull CachingStrategy strategy) {
        super(strategy);
    }

    protected abstract Connection getConnection() throws SQLException;

    @Override
    protected V load(@NotNull K key) throws Exception {
        final Connection connection = getConnection();

        try (connection) {
            return load(connection, key);
        }
    }

    protected abstract V load(Connection connection, @NotNull K key) throws Exception;

    @Override
    protected Map<K, V> load(@NotNull List<K> keys) throws Exception {
        final Connection connection = getConnection();

        try (connection) {
            return load(connection, keys);
        }
    }

    protected Map<K, V> load(Connection connection, @NotNull List<K> list) throws Exception {
        Map<K, V> map = new HashMap<>(list.size());

        for (K key : list) {
            map.put(key, load(connection, key));
        }

        return map;
    }
}
