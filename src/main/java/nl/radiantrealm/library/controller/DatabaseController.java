package nl.radiantrealm.library.controller;

import com.zaxxer.hikari.HikariDataSource;
import nl.radiantrealm.library.ApplicationService;

import java.sql.Connection;

public abstract class DatabaseController implements ApplicationService {
    private static HikariDataSource dataSource;

    public DatabaseController() {}

    //Start/stop system to be re-implemented in next branch.

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
