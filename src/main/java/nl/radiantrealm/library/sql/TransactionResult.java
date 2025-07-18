package nl.radiantrealm.library.sql;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.UUID;

public record TransactionResult(ResultSet rs) implements AutoCloseable {

    @Override
    public void close() throws Exception {
        rs.close();
    }

    public BigDecimal getBigDecimal(String name) throws Exception {
        return rs.getBigDecimal(name);
    }

    public Integer getInteger(String name) throws Exception {
        return rs.getInt(name);
    }

    public String getString(String name) throws Exception {
        return rs.getString(name);
    }

    public UUID getUUID(String name) throws Exception {
        return UUID.fromString(rs.getString(name));
    }
}
