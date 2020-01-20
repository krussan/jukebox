package se.qxx.jukebox.settings;

public class ImdbSettingsTest {
    private String sleepSecondsMin;
    private String sleepSecondsMax;
    private boolean debug;

    public String getSleepSecondsMin() {
        return sleepSecondsMin;
    }

    public int getSleepSecondsMinInt() {
        try {
            return Integer.parseInt(this.getSleepSecondsMin());
        }
        catch (Exception e) {
            return 1;
        }
    }

    public void setSleepSecondsMin(String sleepSecondsMin) {
        this.sleepSecondsMin = sleepSecondsMin;
    }

    public String getSleepSecondsMax() {
        return sleepSecondsMax;
    }

    public int getSleepSecondsMaxInt() {
        try {
            return Integer.parseInt(this.getSleepSecondsMax());
        }
        catch (Exception e) {
            return 1;
        }
    }

    public void setSleepSecondsMax(String sleepSecondsMax) {
        this.sleepSecondsMax = sleepSecondsMax;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
