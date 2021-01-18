package se.qxx.jukebox.settings;

public class BuildersTest {
    private String executor;
    private boolean enabled;
    private String weight;

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

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getWeightInt() {
        try {
            return Integer.parseInt(this.getWeight());
        }
        catch (Exception e) {
            return 1;
        }
    }
}
