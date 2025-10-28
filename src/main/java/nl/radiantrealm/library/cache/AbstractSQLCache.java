package nl.radiantrealm.library.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractSQLCache<K, V> extends AbstractCache<K, V> {

    public AbstractSQLCache(CachingStrategy strategy) {
        super(strategy);
    }

    protected abstract Connection getConnection() throws SQLException;

    @Override
    protected V load(K key) throws Exception {
        final Connection connection = getConnection();

        try (connection) {
            return load(connection, key);
        }
    }

    protected abstract V load(Connection connection, K key) throws Exception;

    @Override
    protected HashMap<K, V> load(ArrayList<K> keys) throws Exception {
        final Connection connection = getConnection();

        try (connection) {
            return load(connection, keys);
        }
    }

    protected HashMap<K, V> load(Connection connection, ArrayList<K> list) throws Exception {
        HashMap<K, V> map = new HashMap<>(list.size());

        for (K key : list) {
            map.put(key, load(connection, key));
        }

        return map;
    }
}
