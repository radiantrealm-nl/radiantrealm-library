package nl.radiantrealm.library.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariDatabase implements Database {
    private final HikariDataSource dataSource;

    public HikariDatabase(HikariConfig config) {
        this.dataSource = new HikariDataSource(config);
    }

    public static HikariConfig createDefaultConfiguration(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return config;
    }

    public static HikariConfig createSQLiteConfiguration(String filePath) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:sqlite:%s", filePath));
        return config;
    }

    @Override
    public void connect() throws SQLException {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(60)) {
                throw new SQLException("Failed to validate the database connection");
            }
        }
    }

    @Override
    public void disconnect() {
        dataSource.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
