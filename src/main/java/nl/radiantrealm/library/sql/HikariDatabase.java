package nl.radiantrealm.library.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.radiantrealm.library.ApplicationService;

import java.sql.Connection;

public abstract class HikariDatabase implements ApplicationService {
    protected static HikariDataSource dataSource;

    protected abstract String databaseURL();
    protected abstract String databaseUsername();
    protected abstract String databasePassword();

    public HikariDatabase() {
        HikariDatabase.dataSource = null;
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

    @Override
    public void start() throws Exception {
        dataSource = buildDataSource();
        ApplicationService.super.start();
    }

    @Override
    public void stop() throws Exception {
        dataSource.close();
        dataSource = null;
        ApplicationService.super.stop();
    }

    public static Connection getConnection(boolean autoCommit) throws Exception {
        if (dataSource == null) {
            throw new Exception("Database offline.");
        }

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    public static Connection getConnection(Connection connection) throws Exception {
        return getConnection(false);
    }
}
