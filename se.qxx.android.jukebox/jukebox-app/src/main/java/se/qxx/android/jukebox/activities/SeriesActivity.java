package se.qxx.android.jukebox.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import se.qxx.android.jukebox.ChromeCastConfiguration;
import se.qxx.android.jukebox.R;

public class SeriesActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        ChromeCastConfiguration.initialize(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(getMenuInflater(), menu);

        return true;
    }

}
