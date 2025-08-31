package nl.radiantrealm.library.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.radiantrealm.library.ApplicationService;

import java.sql.Connection;

public abstract class DatabaseController implements ApplicationService {
    protected static HikariDataSource dataSource;

    protected abstract String databaseURL();
    protected abstract String databaseUsername();
    protected abstract String databasePassword();

    @Override
    public void start() {
        ApplicationService.super.start();

        if (dataSource == null) {
            dataSource = setupDataSource();
        }
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();

        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    protected HikariDataSource setupDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseURL());
        config.setUsername(databaseUsername());
        config.setPassword(databasePassword());
        config.setMaximumPoolSize(10);
        config.setIdleTimeout(30000);
        return new HikariDataSource(config);
    }

    public static Connection getConnection(boolean autoCommit) throws Exception {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    public static Connection getConnection() throws Exception {
        return getConnection(false);
    }

    public static void commit(Connection connection) throws Exception {
        connection.commit();
    }

    public static void rollback(Connection connection) throws Exception {
        connection.rollback();
    }
}
