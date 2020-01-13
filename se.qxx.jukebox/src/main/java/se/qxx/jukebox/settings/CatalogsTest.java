package se.qxx.jukebox.settings;

import java.util.List;

public class CatalogsTest {
    private String path;
    private boolean includeSubDirectories;
    private List<String> extensions;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIncludeSubDirectories() {
        return includeSubDirectories;
    }

    public void setIncludeSubDirectories(boolean includeSubDirectories) {
        this.includeSubDirectories = includeSubDirectories;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }
}
