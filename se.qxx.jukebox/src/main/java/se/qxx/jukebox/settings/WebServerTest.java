package se.qxx.jukebox.settings;

import se.qxx.protodb.model.CaseInsensitiveMap;

import java.util.Map;

public class WebServerTest {
    private CaseInsensitiveMap mimeTypeMap;
    private CaseInsensitiveMap extensionOverrideMap;

    public CaseInsensitiveMap getMimeTypeMap() {
        return mimeTypeMap;
    }

    public void setMimeTypeMap(CaseInsensitiveMap mimeTypeMap) {
        this.mimeTypeMap = mimeTypeMap;
    }

    public CaseInsensitiveMap getExtensionOverrideMap() {
        return extensionOverrideMap;
    }

    public void setExtensionOverrideMap(CaseInsensitiveMap extensionOverrideMap) {
        this.extensionOverrideMap = extensionOverrideMap;
    }
}
