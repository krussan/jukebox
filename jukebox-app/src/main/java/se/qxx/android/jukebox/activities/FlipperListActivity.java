package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.cast.framework.CastContext;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.PagerFragment;
import se.qxx.android.jukebox.activities.fragments.SearchFragment;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragment;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.domain.JukeboxDomain;

public class FlipperListActivity
    extends BaseActivity
    implements JukeboxFragment.JukeboxFragmentHandler {


    private ViewMode mode = ViewMode.Movie;
    private JukeboxSettings settings;
    private JukeboxConnectionHandler connectionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new JukeboxSettings(this);
        setupConnectionHandler(false);

		setContentView(R.layout.jukebox_main_wrapper);
		//TOOD: Load main fragment
        PagerFragment pagerFragment = PagerFragment.newInstance();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.rootJukeboxMainWrapper, pagerFragment);
        ft.commit();
    }

    private void setupConnectionHandler(boolean reAttachCallbacks) {
        if (this.connectionHandler == null) {
            connectionHandler = new JukeboxConnectionHandler(
                    settings.getServerIpAddress(),
                    settings.getServerPort());

            if (reAttachCallbacks) {
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (f instanceof JukeboxConnectionHandler.ConnectorCallbackEventListener)
                        connectionHandler.addCallback((JukeboxConnectionHandler.ConnectorCallbackEventListener) f);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.connectionHandler.stop();
        this.connectionHandler = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupConnectionHandler(true);
    }

    @Override
    public JukeboxConnectionHandler getConnectionHandler() {
        return this.connectionHandler;
    }

    @Override
    public void switchFragment(Fragment fragment) {
        super.switchFragment(R.id.rootJukeboxMainWrapper, fragment);
    }

    @Override
    public void switchFragment(ViewMode newMode, JukeboxDomain.Series series, JukeboxDomain.Season season) {
        super.switchFragment(R.id.rootJukeboxMainWrapper, newMode, series, season);
    }


//    private JukeboxFragment getCurrentFragment() {
//        int position = pager.getCurrentItem();
//        return (JukeboxFragment)adapter.getRegisteredFragment(position);
//    }



}
