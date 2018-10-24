package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastContext;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;

public class SeriesActivity extends AppCompatActivity {
    private CastContext mCastContext;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        mCastContext = CastContext.getSharedInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

        return true;
    }

}
