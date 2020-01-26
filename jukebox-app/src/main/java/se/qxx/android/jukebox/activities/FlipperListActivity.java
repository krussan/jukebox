package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.cast.framework.CastContext;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragment;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.settings.JukeboxSettings;

public class FlipperListActivity extends AppCompatActivity implements JukeboxFragment.JukeboxFragmentHandler, SearchView.OnQueryTextListener {
	ViewPager pager;
	private CastContext mCastContext;
    private ViewMode mode = ViewMode.Movie;
    private JukeboxSettings settings;
    private JukeboxConnectionHandler connectionHandler;

    protected View getRootView() {
		return findViewById(R.id.rootJukeboxViewPager);
	}

	public final int VIEWMODE_MOVIE = 0;
	public final int VIEWMODE_SERIES = 1;

    public ViewMode getMode() {
        return mode;
    }

    public void setMode(ViewMode mode) {
        this.mode = mode;
    }

    private ViewMode getViewMode(int position) {
	    if (position == VIEWMODE_MOVIE)
	        return ViewMode.Movie;
	    else
	        return this.getMode();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new JukeboxSettings(this);
        setupConnectionHandler(false);

        ChromeCastConfiguration.checkGooglePlayServices(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
			this.setMode((ViewMode) b.getSerializable("mode"));
		}

		setContentView(R.layout.jukebox_main_wrapper);
        pager = (ViewPager)this.getRootView();

        JukeboxFragmentAdapter mfa = new JukeboxFragmentAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(mfa);

		mCastContext = CastContext.getSharedInstance(this);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu, this);


		return true;
	}

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.prefs_menu_item:
                Intent intentPreferences = new Intent(this, JukeboxPreferenceActivity.class);
                startActivity(intentPreferences);
                break;

        }
        return true;
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
