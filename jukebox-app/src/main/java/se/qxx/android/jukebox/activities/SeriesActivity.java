package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;

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

    @Override
    public void onResume() {
        super.onResume();

        ChromeCastConfiguration.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        ChromeCastConfiguration.onPause();
    }

}
