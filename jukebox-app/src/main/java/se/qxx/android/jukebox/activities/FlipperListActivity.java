package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.Connector;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;

public class FlipperListActivity extends AppCompatActivity {
	ViewPager pager;
	private CastContext mCastContext;
    private ViewMode mode = ViewMode.Movie;


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

		JukeboxSettings.init(this);

        if (getIntent() != null && getIntent().getExtras() != null)
            this.setMode((ViewMode)getIntent().getExtras().getSerializable("mode"));

		setContentView(R.layout.jukebox_main_wrapper);
        pager = (ViewPager)this.getRootView();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
			    Model.get().setOffset(0);
                Connector.connect(
                        0,
                        Model.get().getNrOfItems(),
                        ViewMode.getModelType(getViewMode(position)));
            }

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

        JukeboxFragmentAdapter mfa = new JukeboxFragmentAdapter(getSupportFragmentManager(), this.getMode(), this);

        pager.setAdapter(mfa);
        pager.setCurrentItem(Model.get().getCurrentMovieIndex());

		mCastContext = CastContext.getSharedInstance(this);

        if (this.getMode() == ViewMode.Season) {
            // trigger an update if we are on a season list
            Connector.connect(
                    0,
                    Model.get().getNrOfItems(),
                    ViewMode.getModelType(this.getMode()));
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
	}

	public void onButtonClicked(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btnPlay:
				Intent iPlay = new Intent(this, NowPlayingActivity.class);
				iPlay.putExtra("mode", ViewMode.Movie);
				startActivity(iPlay);
				break;	
			case R.id.btnViewInfo:
				String url = Model.get().getCurrentMovie().getImdbUrl();
				if (url != null && url.length() > 0) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
				else {
					Toast.makeText(v.getContext(), "No IMDB link available", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}

}
