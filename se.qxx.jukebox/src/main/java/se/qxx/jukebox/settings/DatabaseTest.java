package se.qxx.jukebox.settings;

public class DatabaseTest {
    private String driver;
    private String connectionString;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
