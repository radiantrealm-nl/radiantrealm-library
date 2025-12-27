package nl.radiantrealm.library.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {
    void connect() throws SQLException;
    void disconnect() throws SQLException;
    Connection getConnection() throws SQLException;
}
