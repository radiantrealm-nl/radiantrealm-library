package nl.radiantrealm.library.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.ApplicationStatus;

import java.sql.Connection;

public abstract class DatabaseController implements ApplicationService {
    private static HikariDataSource dataSource;

    public DatabaseController() {}

    @Override
    public void start() throws Exception {
        try {
            if (!status().equals(ApplicationStatus.STOPPED)) return;

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(setDatabaseURL());
            config.setUsername(System.getenv(setDatabaseUsername()));
            config.setPassword(System.getenv(setDatabasePassword()));
            config.setMaximumPoolSize(10);
            config.setAutoCommit(false);
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new Exception("Failed to setup database connection.", e);
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            if (status().equals(ApplicationStatus.STOPPED)) return;

            dataSource.close();
        } catch (Exception e) {
            throw new Exception("Failed to stop database.", e);
        }
    }

    @Override
    public ApplicationStatus status() throws Exception {
        try {
            if (dataSource.isRunning()) {
                return ApplicationStatus.RUNNING;
            } else {
                return ApplicationStatus.STOPPED;
            }
        } catch (Exception e) {
            throw new Exception("Failed to retrieve database status.", e);
        }
    }

    protected abstract String setDatabaseURL();

    protected abstract String setDatabaseUsername();

    protected abstract String setDatabasePassword();

    public static Connection getConnection(boolean autoCommiit) throws Exception {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommiit);
        return connection;
    }

    public static Connection getConnection() throws Exception {
        return getConnection(false);
    }

    public static void commit(Connection connection) throws Exception {
        connection.commit();
    }

    public static void rollback(Connection connection) throws Exception {
        connection.commit();
    }
}
