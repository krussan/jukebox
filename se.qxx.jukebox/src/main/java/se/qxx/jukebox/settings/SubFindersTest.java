package se.qxx.jukebox.settings;

import java.util.List;

public class SubFindersTest {
    private String path;
    private String threadWaitSeconds;

    private List<FindersTest> finders;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FindersTest> getFinders() {
        return finders;
    }

    public void setFinders(List<FindersTest> finders) {
        this.finders = finders;
    }

    public String getThreadWaitSeconds() {
        return threadWaitSeconds;
    }

    public void setThreadWaitSeconds(String threadWaitSeconds) {
        this.threadWaitSeconds = threadWaitSeconds;
    }

    public int getThreadWaitSecondsInt() {
        try {
            return Integer.parseInt(this.getThreadWaitSeconds());
        }
        catch (Exception e) {
            return 1;
        }
    }
}
