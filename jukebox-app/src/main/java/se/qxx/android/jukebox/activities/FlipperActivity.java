package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.dialogs.ActionDialog;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.detail.MovieFragmentAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;

import java.util.ArrayList;
import java.util.List;

public class FlipperActivity extends AppCompatActivity implements OnPageChangeListener, OnLongClickListener {
	ViewPager pager;
	private CastContext mCastContext;

	private ViewMode mode = ViewMode.Movie;
	protected View getRootView() {
		return findViewById(R.id.rootViewPager);
	}
	private int currentPosition = 0;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemwrapper);
        pager = (ViewPager)this.getRootView();

        MovieFragmentAdapter mfa = new MovieFragmentAdapter(getSupportFragmentManager(), this.getMovies());

        pager.setAdapter(mfa);
        pager.setCurrentItem(getPosition());
        
        this.getRootView().setOnLongClickListener(this);

        mCastContext = CastContext.getSharedInstance(this);

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
				iPlay.putExtra("mode", this.getMode());
				startActivity(iPlay);
				break;	
			case R.id.btnViewInfo:
				String url = this.getCurrentMovie().getImdbUrl();
				if (url != null && url.length() > 0) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
				else {
					Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		this.setCurrentPosition(arg0);
	}

	@Override
	public boolean onLongClick(View v) {
		ActionDialog d = new ActionDialog(
		        this,
                this.getCurrentMovie().getID(),
                RequestType.TypeMovie);

		d.show();
		return false;
	}

	private int getPosition() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
			return b.getInt("position");
		}

		return 0;
	}

	private ViewMode getMode() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
			return (ViewMode)b.getSerializable("mode");
		}

		return ViewMode.Movie;
	}


	private List<JukeboxDomain.Movie> getMovies() {
	    Bundle b = getIntent().getExtras();

	    if (b != null) {
	        return (List<JukeboxDomain.Movie>)b.getSerializable("movies");
        }

        return new ArrayList<JukeboxDomain.Movie>();
    }

    private JukeboxDomain.Movie getCurrentMovie() {
        return this.getMovies().get(this.getCurrentPosition());
    }

}
