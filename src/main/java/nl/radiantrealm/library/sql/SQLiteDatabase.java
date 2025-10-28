package nl.radiantrealm.library.sql;

public abstract class SQLiteDatabase extends HikariDatabase {
    private final String filePath;

    public SQLiteDatabase(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected String databaseURL() {
        return String.format("jdbc:sqlite:%s", filePath);
    }

    @Override
    protected String databaseUsername() {
        return null;
    }

    @Override
    protected String databasePassword() {
        return null;
    }
}
