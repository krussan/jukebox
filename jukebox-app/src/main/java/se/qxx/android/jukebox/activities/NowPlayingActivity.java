package se.qxx.android.jukebox.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.LocalPlayerFragment;
import se.qxx.android.jukebox.activities.fragments.PlayerFragment;
import se.qxx.android.jukebox.cast.CastProvider;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;

public class NowPlayingActivity extends AppCompatActivity {
    private static boolean screenChange = false;

    private ViewMode getMode() {
        Intent i = getIntent();
        if (i != null) {
            Bundle b = i.getExtras();
            if (b != null) {
                return (ViewMode)b.getSerializable("mode");
            }
        }
        return ViewMode.Movie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nowplaying_container);

        initializeView();

        screenChange = false;
    }

    public void initializeView() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragmentContainer, PlayerFragment.newInstance(CastProvider.isLocalPlayer(), screenChange));
        ft.commit();

    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        if (seeker != null)
            seeker.stop();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (seeker != null)
            seeker.stop();

        if (castProvider != null && isLocalPlayer())
            castProvider.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (seeker != null)
            seeker.start();
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);

        screenChange = true;
    }

}
