package se.qxx.jukebox.settings;

import java.util.List;

public class SettingsTest {
    private List<CatalogsTest> catalogs;
    private SubFindersTest subfinders;
    private List<BuildersTest> builders;
    private String port;
    private List<LogsTest> logs;
    private DatabaseTest database;
    private WebServerTest webserver;
    private ConverterTest converter;


    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getPortInt() {
        try {
            return Integer.parseInt(this.getPort());
        }
        catch (Exception e) {
            return 45444;
        }
    }
    public List<LogsTest> getLogs() {
        return logs;
    }

    public void setLogs(List<LogsTest> logs) {
        this.logs = logs;
    }

    public DatabaseTest getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseTest database) {
        this.database = database;
    }

    public List<CatalogsTest> getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(List<CatalogsTest> catalogs) {
        this.catalogs = catalogs;
    }

    public WebServerTest getWebserver() {
        return webserver;
    }

    public void setWebserver(WebServerTest webserver) {
        this.webserver = webserver;
    }

    public List<BuildersTest> getBuilders() {
        return builders;
    }

    public void setBuilders(List<BuildersTest> builders) {
        this.builders = builders;
    }

    public SubFindersTest getSubfinders() {
        return subfinders;
    }

    public void setSubfinders(SubFindersTest subfinders) {
        this.subfinders = subfinders;
    }

    public ConverterTest getConverter() {
        return converter;
    }

    public void setConverter(ConverterTest converter) {
        this.converter = converter;
    }
}
