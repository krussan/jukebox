package se.qxx.android.jukebox.activities;

import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.PlayerFragment;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;

public class NowPlayingActivity extends BaseActivity {
    private static boolean screenChange = false;
    private JukeboxSettings settings;


    @Override
    protected int getSearchContainer() {
        return R.id.fragmentContainer;
    }

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

        settings = new JukeboxSettings(this);
        initializeView();

        screenChange = false;
    }

    public void initializeView() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragmentContainer,
                PlayerFragment.newInstance(
                        ChromeCastConfiguration.getCastType(),
                        screenChange));
        ft.commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);

        screenChange = true;
    }


}
