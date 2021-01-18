package se.qxx.jukebox.settings;

public class ImdbTitleTest {
    private String preferredLanguage;
    private boolean  useOriginalIfExists;

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isUseOriginalIfExists() {
        return useOriginalIfExists;
    }

    public void setUseOriginalIfExists(boolean useOriginalIfExists) {
        this.useOriginalIfExists = useOriginalIfExists;
    }
}
