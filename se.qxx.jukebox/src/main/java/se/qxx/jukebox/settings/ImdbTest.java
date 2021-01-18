package se.qxx.jukebox.settings;

import java.util.List;

public class ImdbTest {
    private ImdbSettingsTest settings;
    private ImdbTitleTest title;
    private String searchUrl;
    private List<String> datePatterns;

    public ImdbSettingsTest getSettings() {
        return settings;
    }

    public void setSettings(ImdbSettingsTest settings) {
        this.settings = settings;
    }

    public ImdbTitleTest getTitle() {
        return title;
    }

    public void setTitle(ImdbTitleTest title) {
        this.title = title;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public List<String> getDatePatterns() {
        return datePatterns;
    }

    public void setDatePatterns(List<String> datePatterns) {
        this.datePatterns = datePatterns;
    }
}
