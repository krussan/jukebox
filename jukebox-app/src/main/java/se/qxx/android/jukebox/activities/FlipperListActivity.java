package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.cast.framework.CastContext;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;

public class FlipperListActivity extends AppCompatActivity {
	ViewPager pager;
	private CastContext mCastContext;
    private ViewMode mode = ViewMode.Movie;
    private JukeboxSettings settings;


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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
	}



}
