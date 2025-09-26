package nl.radiantrealm.library.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public abstract class HikariDatabase {
    protected static HikariDataSource dataSource;

    protected abstract String databaseURL();
    protected abstract String databaseUsername();
    protected abstract String databasePassword();

    public HikariDatabase() {
        HikariDatabase.dataSource = buildDataSource();
    }

    protected HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseURL());
        config.setUsername(databaseUsername());
        config.setPassword(databasePassword());
        config.setMaximumPoolSize(10);
        config.setIdleTimeout(30000);
        return new HikariDataSource(config);
    }

    public static Connection getConnection(boolean autoCommit) throws Exception {
        if (dataSource == null) {
            throw new Exception("Database offline.");
        }

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    public static Connection getConnection() throws Exception {
        return getConnection(false);
    }
}
