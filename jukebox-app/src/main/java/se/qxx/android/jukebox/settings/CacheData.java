package se.qxx.android.jukebox.settings;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class CacheData {

    private static final String CACHE_FILE_NAME = "tazmo_cache.json";
    private static final String TAG = "CacheData";
    private Context context;

    public CacheData(Context context) {
        this.context = context;
    }

    public void saveMediaState(int mediaID, int duration) {
        try {
            JSONObject json = new JSONObject(getJsonFile());
            json.put(Integer.toString(mediaID), duration);

            saveCacheFile(json);
        }
        catch (IOException|JSONException iox) {
            Log.e(TAG, "Error when saving cache data", iox);
        }
    }

    public int getMediaState(int mediaID) {
        try {
            JSONObject json = new JSONObject(getJsonFile());
            String key = Integer.toString(mediaID);
            if (json.has(key))
                return Integer.parseInt(json.get(key).toString());
        }
        catch (IOException|JSONException iox) {
            Log.e(TAG, "Error when getting cache data", iox);
        }

        return 0;

    }

    private void saveCacheFile(JSONObject json) throws IOException {
        File file = getCacheFile();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "utf-8"))) {
            writer.write(json.toString());
        }
    }

    private String getJsonFile() throws IOException {
        File cacheFile = getCacheFile();
        if (cacheFile != null && cacheFile.exists()) {
            FileInputStream fs = new FileInputStream(cacheFile);
            StringBuilder sb = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(fs))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        }

        return "{}";
    }

    private File getCacheFile() {
        File cacheDir = this.context.getCacheDir();
        if (cacheDir != null) {
            if (cacheDir.mkdirs() || (cacheDir.exists() && cacheDir.isDirectory())) {
                return new File(cacheDir, CACHE_FILE_NAME);
            }
        }

        return null;
    }
}
