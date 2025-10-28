package nl.radiantrealm.library.sql;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDatabase {

    public abstract void connect();
    public abstract void disconnect();

    protected abstract String databaseURL();
    protected abstract String databaseUsername();
    protected abstract String databasePassword();

    public abstract Connection getConnection() throws SQLException;
}
