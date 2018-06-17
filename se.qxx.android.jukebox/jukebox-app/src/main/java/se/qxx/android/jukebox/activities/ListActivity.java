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

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.list.EpisodeLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeasonLayoutAdapter;
import se.qxx.android.jukebox.adapters.support.EndlessScrollListener;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.Connector;
import se.qxx.android.jukebox.dialogs.ActionDialog;
import se.qxx.android.jukebox.model.Constants;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.android.jukebox.adapters.support.IOffsetHandler;

public class ListActivity extends AppCompatActivity implements
    AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, Connector.ConnectorCallbackEventListener, IOffsetHandler {

    private final int NR_OF_ITEMS = 15;

	private CastContext mCastContext;
	private SeasonLayoutAdapter _seasonLayoutAdapter;
	private EpisodeLayoutAdapter _episodeLayoutAdapter;
	private EndlessScrollListener scrollListener;
	private ViewMode mode;
	private int offset;
	private JukeboxDomain.Series series;
	private JukeboxDomain.Season season;

    protected View getRootView() {
		return findViewById(R.id.rootMain);
	}

	public ViewMode getMode() {
		return mode;
	}

	public void setMode(ViewMode mode) {
		this.mode = mode;
	}

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public JukeboxDomain.Series getSeries() {
        return series;
    }

    public void setSeries(JukeboxDomain.Series series) {
        this.series = series;
    }

    public JukeboxDomain.Season getSeason() {
        return season;
    }

    public void setSeason(JukeboxDomain.Season season) {
        this.season = season;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JukeboxDomain.Series series = (JukeboxDomain.Series)getIntent().getExtras().getSerializable("series");
        JukeboxDomain.Season season = (JukeboxDomain.Season)getIntent().getExtras().getSerializable("season");

		JukeboxSettings.init(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            this.setMode(
                    (ViewMode) getIntent().getExtras().getSerializable("mode"));
            this.setSeries((JukeboxDomain.Series) b.getSerializable("series"));
            this.setSeason((JukeboxDomain.Season) b.getSerializable("season"));
        }

		setContentView(R.layout.main);
		initializeView();

		mCastContext = CastContext.getSharedInstance(this);

		loadMoreData(0);
    }

	@Override
	public boolean onCreateOptionsMenu(
	        Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
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
				    int offset = page * Constants.NR_OF_ITEMS;
					this.getHandler().setOffset(offset);

					if (this.getHandler().getMode() == ViewMode.Season)
					    loadMoreData(
					            offset,
                                this.getHandler().getSeries().getID());
					else if (this.getHandler().getMode() == ViewMode.Episode)
                        loadMoreData(
                                offset,
                                this.getHandler().getSeries().getID(),
                                this.getHandler().getSeason().getID());
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

    private void loadMoreData(int offset, int seriesID, int seasonID) {
        Connector.connect(offset, NR_OF_ITEMS, ViewMode.getModelType(this.getMode()), seriesID, seasonID);
    }

    private void loadMoreData(int offset, int seriesID) {
        Connector.connect(offset, NR_OF_ITEMS, ViewMode.getModelType(this.getMode()), seriesID, -1);
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (this.getMode() == ViewMode.Season) {
            Model.get().setCurrentSeason(pos); //bundle the
            Intent intentSeries = new Intent(this, ListActivity.class);
            intentSeries.putExtra("mode", ViewMode.Episode);
            intentSeries.putExtra("season", )

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
                if (_seasonLayoutAdapter != null) {
                    _seasonLayoutAdapter.setSeasons(seasons);
                   _seasonLayoutAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_episodeLayoutAdapter != null) {
                    _episodeLayoutAdapter.setEpisodes(seasons);
                    _episodeLayoutAdapter.notifyDataSetChanged();
                }
            }
        });
    }



}
