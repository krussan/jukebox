package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.cast.framework.CastContext;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;

public class SeriesActivity extends AppCompatActivity {
    private CastContext mCastContext;
    private JukeboxSettings settings;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        settings = new JukeboxSettings(this);
        mCastContext = CastContext.getSharedInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu, settings.getCurrentMediaPlayer());

        return true;
    }

}
