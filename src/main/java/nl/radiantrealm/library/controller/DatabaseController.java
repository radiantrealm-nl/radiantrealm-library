package nl.radiantrealm.library.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public abstract class DatabaseController {
    private static HikariDataSource dataSource;

    public DatabaseController() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/bankconomy");
        config.setUsername(System.getenv("DB_USERNAME"));
        config.setPassword(System.getenv("DB_PASSWORD"));
        config.setMaximumPoolSize(10);
        config.setAutoCommit(false);
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection(boolean autoCommiit) throws Exception {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommiit);
        return connection;
    }

    public static void commit(Connection connection) throws Exception {
        connection.commit();
    }

    public static void rollback(Connection connection) throws Exception {
        connection.commit();
    }
}
