package nl.radiantrealm.library.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public record TransactionBuilder(PreparedStatement statement) implements AutoCloseable {

    @Override
    public void close() throws Exception {
        statement.close();
    }

    public static TransactionBuilder prepare(Connection connection, String sql) throws Exception {
        return new TransactionBuilder(connection.prepareStatement(sql));
    }

    public void execute() throws Exception {
        statement.execute();
    }

    public TransactionResult executeQuery() throws Exception {
        return new TransactionResult(statement.executeQuery());
    }

    public TransactionBuilder setBigDecimal(int index, BigDecimal bigDecimal) throws Exception {
        statement.setBigDecimal(index, bigDecimal);
        return this;
    }

    public TransactionBuilder setString(int index, String string) throws Exception {
        statement.setString(index, string);
        return this;
    }

    public TransactionBuilder setUUID(int index, UUID uuid) throws Exception {
        statement.setString(index, uuid.toString());
        return this;
    }
}
