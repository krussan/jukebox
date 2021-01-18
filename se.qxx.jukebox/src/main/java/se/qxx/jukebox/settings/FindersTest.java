package se.qxx.jukebox.settings;

import se.qxx.protodb.model.CaseInsensitiveMap;

import java.util.Map;

public class FindersTest {
    private String executor;
    private boolean enabled;
    private CaseInsensitiveMap settings;

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CaseInsensitiveMap getSettings() {
        return settings;
    }

    public void setSettings(CaseInsensitiveMap settings) {
        this.settings = settings;
    }
}
