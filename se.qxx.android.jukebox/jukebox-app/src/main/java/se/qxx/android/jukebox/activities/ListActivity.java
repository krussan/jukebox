package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;

import java.util.EventObject;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.list.EpisodeLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeasonLayoutAdapter;
import se.qxx.android.jukebox.adapters.support.EndlessScrollListener;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.Connector;
import se.qxx.android.jukebox.dialogs.ActionDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class ListActivity extends AppCompatActivity implements
    AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, Connector.ConnectorCallbackEventListener {

	private CastContext mCastContext;
	private SeasonLayoutAdapter _seasonLayoutAdapter;
	private EpisodeLayoutAdapter _episodeLayoutAdapter;
	private EndlessScrollListener scrollListener;
	private ViewMode mode;

    protected View getRootView() {
		return findViewById(R.id.rootMain);
	}

	public ViewMode getMode() {
		return mode;
	}

	public void setMode(ViewMode mode) {
		this.mode = mode;
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		JukeboxSettings.init(this);

        if (getIntent() != null && getIntent().getExtras() != null)
            this.setMode((ViewMode)getIntent().getExtras().getSerializable("mode"));

		setContentView(R.layout.main);
		initializeView();

		mCastContext = CastContext.getSharedInstance(this);

		loadMoreData(0);
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

	private void initializeView() {
		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		findViewById(R.id.btnRefresh).setOnClickListener(this);
		findViewById(R.id.btnSelectMediaPlayer).setOnClickListener(this);
	    findViewById(R.id.btnCurrentMovie).setOnClickListener(this);
		findViewById(R.id.btnPreferences).setOnClickListener(this);
		findViewById(R.id.btnOn).setOnClickListener(this);
		findViewById(R.id.btnOff).setOnClickListener(this);

		scrollListener = new EndlessScrollListener() {
			@Override
			public boolean onLoadMore(int page, int totalItemsCount) {
				if (!Model.get().isLoading()) {
					Model.get().setLoading(true);
					Model.get().setOffset(page * Model.get().getNrOfItems());
					loadMoreData(page * Model.get().getNrOfItems());
				}

				return true;
			}
		};

		lv.setOnScrollListener(scrollListener);

        if (this.getMode() == ViewMode.Season) {
            _seasonLayoutAdapter = new SeasonLayoutAdapter(this.getApplicationContext(), Model.get().getCurrentSeries());
            lv.setAdapter(_seasonLayoutAdapter);
        }
        else if (this.getMode() == ViewMode.Episode) {
            _episodeLayoutAdapter = new EpisodeLayoutAdapter(this.getApplicationContext(), Model.get().getCurrentSeason());
            lv.setAdapter(_episodeLayoutAdapter);
        }

        Connector.setupOnOffButton(this.getRootView());
	}

    private void loadMoreData(int offset) {
        Connector.connect(offset, Model.get().getNrOfItems(), ViewMode.getModelType(this.getMode()));
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (this.getMode() == ViewMode.Season) {
            Model.get().setCurrentSeason(pos);
            Intent intentSeries = new Intent(this, ListActivity.class);
            intentSeries.putExtra("mode", ViewMode.Episode);

            startActivity(intentSeries);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

        ActionDialog d = null;

        if (this.getMode() == ViewMode.Movie) {
            d = new ActionDialog(this, Model.get().getMovie(arg2).getID(), JukeboxDomain.RequestType.TypeMovie);
        }
        else if (this.getMode() == ViewMode.Series) {
            d = new ActionDialog(this, Model.get().getSeries(arg2).getID(), JukeboxDomain.RequestType.TypeSeries);
        }

        if (d != null)
            d.show();

        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnRefresh:
                Logger.Log().i("onConnectClicked");

                Model.get().setOffset(0);
                Model.get().clearMovies();
                Model.get().clearSeries();

                Connector.connect(Model.get().getOffset(), Model.get().getNrOfItems(), ViewMode.getModelType(this.getMode()));

                break;
            case R.id.btnSelectMediaPlayer:
                Logger.Log().i("selectMediaPlayerClicked");

                Intent i = new Intent(this, PlayerPickerActivity.class);
                startActivity(i);
                break;
            case R.id.btnOn:
            case R.id.btnOff:
                Connector.onoff(this);
                Connector.setupOnOffButton(getRootView());
                break;
            case R.id.btnPreferences:
                Intent intentPreferences = new Intent(this, JukeboxPreferenceActivity.class);
                startActivity(intentPreferences);
                break;
            default:
                break;

        }
    }

    @Override
    public void handleMoviesUpdated(List<JukeboxDomain.Movie> movies) {
        //should not happen in this activity
    }

    @Override
    public void handleSeriesUpdated(List<JukeboxDomain.Series> series) {
        //should not happen in this activity
    }
    private Runnable modelResultUpdatedRunnable = new Runnable() {

        @Override
        public void run() {
            if (_seasonLayoutAdapter != null)
                _seasonLayoutAdapter.notifyDataSetChanged();

            if (_episodeLayoutAdapter != null)
                _episodeLayoutAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void handleSeasonsUpdated(final List<JukeboxDomain.Season> seasons) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _seasonLayoutAdapter.setSeries();
            }
        });
    }

    @Override
    public void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes) {
        runOnUiThread(modelResultUpdatedRunnable);
    }
}
