package nl.radiantrealm.library.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class HikariDatabase extends AbstractDatabase {
    protected HikariDataSource dataSource;

    protected HikariDataSource buildHikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseURL());
        config.setUsername(databaseUsername());
        config.setPassword(databasePassword());
        return new HikariDataSource(config);
    }

    @Override
    public void connect() {
        if (dataSource == null || dataSource.isClosed()) {
            dataSource = buildHikariDataSource();
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(true);
    }

    public Connection getConnection(boolean autoCommit) throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            connect();
        }

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }
}
