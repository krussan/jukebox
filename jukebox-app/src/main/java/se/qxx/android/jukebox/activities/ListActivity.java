package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.cast.framework.CastContext;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.list.EpisodeLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeasonLayoutAdapter;
import se.qxx.android.jukebox.adapters.support.EndlessScrollListener;
import se.qxx.android.jukebox.adapters.support.IOffsetHandler;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.Connector;
import se.qxx.android.jukebox.dialogs.ActionDialog;
import se.qxx.android.jukebox.model.Constants;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class ListActivity extends AppCompatActivity implements
    AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, Connector.ConnectorCallbackEventListener, IOffsetHandler {

    private final int NR_OF_ITEMS = 15;

	private CastContext mCastContext;
	private SeasonLayoutAdapter _seasonLayoutAdapter;
	private EpisodeLayoutAdapter _episodeLayoutAdapter;
	private int offset;
	private boolean firstIsLast = false;
	private int totalItems = 0;
	private Connector connector;

    protected View getRootView() {
		return findViewById(R.id.rootMain);
	}

	public ViewMode getMode() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
		    ViewMode mode = (ViewMode) b.getSerializable("mode");
		    if (mode != null)
		        return mode;
        }

        return ViewMode.Season;
	}

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public JukeboxDomain.Series getSeries() {
        Bundle b = getIntent().getExtras();
        if (b != null)
            return (JukeboxDomain.Series) b.getSerializable("series");

        return null;
    }
    public JukeboxDomain.Season getSeason() {

        Bundle b = getIntent().getExtras();
        if (b != null)
            return (JukeboxDomain.Season) b.getSerializable("season");

        return null;
    }

    public int getSeriesID() {
        if (this.getSeries() != null)
            return this.getSeries().getID();
        else
            return -1;
    }

    public int getSeasonID() {
        if (this.getSeason() != null)
            return this.getSeason().getID();
        else
            return -1;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		JukeboxSettings.init(this);

		setContentView(R.layout.main);
		initializeView();

		mCastContext = CastContext.getSharedInstance(this);
		connector = new Connector(this);

        loadMoreData(0, getSeriesID(), getSeasonID());
    }

    @Override
    public void onResume() {
        super.onResume();


    }

	@Override
	public boolean onCreateOptionsMenu(
	        Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
	}

	private void initializeView() {
		ListView lv = findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		findViewById(R.id.btnRefresh).setOnClickListener(this);
		findViewById(R.id.btnSelectMediaPlayer).setOnClickListener(this);
	    findViewById(R.id.btnCurrentMovie).setOnClickListener(this);
		findViewById(R.id.btnPreferences).setOnClickListener(this);
		findViewById(R.id.btnOn).setOnClickListener(this);
		findViewById(R.id.btnOff).setOnClickListener(this);

        EndlessScrollListener scrollListener = new EndlessScrollListener(this) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount >= getTotalItems())
                    return false;

                if (!Model.get().isLoading() && !isFirstIsLast()) {
                    int offset = page * Constants.NR_OF_ITEMS;

                    this.getHandler().setOffset(offset);

                    if (this.getHandler().getMode() == ViewMode.Season)
                        loadMoreData(
                                offset,
                                this.getHandler().getSeries().getID());
                    else if (this.getHandler().getMode() == ViewMode.Episode)
                        return false;
                }

                return true;
            }
        };

		lv.setOnScrollListener(scrollListener);

        if (this.getMode() == ViewMode.Season) {
            _seasonLayoutAdapter = new SeasonLayoutAdapter(
                    this.getApplicationContext(),
                    this.getSeries());

            lv.setAdapter(_seasonLayoutAdapter);
        }
        else if (this.getMode() == ViewMode.Episode) {
            _episodeLayoutAdapter =
                new EpisodeLayoutAdapter(
                        this.getApplicationContext(),
                        this.getSeason().getSeasonNumber(),
                        this.getSeason().getEpisodeList());

            lv.setAdapter(_episodeLayoutAdapter);
        }

        connector.setupOnOffButton(this.getRootView());
	}

    private void loadMoreData(int offset, int seriesID) {
        connector.connect(offset, NR_OF_ITEMS, this.getMode(), seriesID, -1);
    }

    private void loadMoreData(int offset, int seriesID, int seasonID) {
        connector.connect(offset, NR_OF_ITEMS, this.getMode(), seriesID, seasonID);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (this.getMode() == ViewMode.Season) {

            Intent intentSeries = new Intent(this, ListActivity.class);
            intentSeries.putExtra("mode", ViewMode.Episode);
            intentSeries.putExtra("series", this.getSeries());
            intentSeries.putExtra("season", (JukeboxDomain.Season)_seasonLayoutAdapter.getItem(pos));

            startActivity(intentSeries);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                   long arg3) {

        ActionDialog d = null;

        if (this.getMode() == ViewMode.Season) {
            d = new ActionDialog(
                    this,
                    this.getSeries().getSeason(position).getID(),
                    JukeboxDomain.RequestType.TypeMovie);
        }
        else if (this.getMode() == ViewMode.Episode) {
            d = new ActionDialog(
                    this,
                    this.getSeason().getEpisode(position).getID(),
                    JukeboxDomain.RequestType.TypeSeries);
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


                clearData();
                loadMoreData(0, getSeriesID(), getSeasonID());

                break;
            case R.id.btnSelectMediaPlayer:
                Logger.Log().i("selectMediaPlayerClicked");

                Intent i = new Intent(this, PlayerPickerActivity.class);
                startActivity(i);
                break;
            case R.id.btnOn:
            case R.id.btnOff:
                connector.onoff(this);
                connector.setupOnOffButton(getRootView());
                break;
            case R.id.btnPreferences:
                Intent intentPreferences = new Intent(this, JukeboxPreferenceActivity.class);
                startActivity(intentPreferences);
                break;
            default:
                break;

        }
    }

    private void clearData() {
        this.setOffset(0);
        ViewMode mode = this.getMode();
        if (mode == ViewMode.Season && _seasonLayoutAdapter != null) {
            _seasonLayoutAdapter.clearSeasons();
            notifySeasons();
        }
        else if (mode == ViewMode.Episode && _episodeLayoutAdapter != null) {
            _episodeLayoutAdapter.clearEpisodes();
            notifyEpisodes();
        }
    }

    @Override
    public void handleMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies) {
        //should not happen in this activity
    }

    @Override
    public void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries) {
        //should not happen in this activity
    }

    @Override
    public void handleSeasonsUpdated(final List<JukeboxDomain.Season> seasons, int totalSeasons) {
        if (this.getMode() == ViewMode.Season) {
            this.setTotalItems(totalSeasons);
            if (_seasonLayoutAdapter != null) {
                _seasonLayoutAdapter.addSeasons(seasons);

                if (this.getOffset() == 0 && seasons.size() <= Constants.NR_OF_ITEMS)
                    this.setFirstIsLast(true);

                notifySeasons();
            }
        }
    }

    @Override
    public void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes, int totalEpisodes) {
        if (this.getMode() == ViewMode.Episode) {
            this.setTotalItems(totalEpisodes);
            if (_episodeLayoutAdapter != null) {
                _episodeLayoutAdapter.addEpisodes(episodes);

                if (this.getOffset() == 0 && episodes.size() <= Constants.NR_OF_ITEMS)
                    this.setFirstIsLast(true);

                notifyEpisodes();
            }
        }
    }

    private void notifySeasons() {
        if (_seasonLayoutAdapter != null)
            runOnUiThread(() -> _seasonLayoutAdapter.notifyDataSetChanged());
    }
    private void notifyEpisodes() {
        if (_episodeLayoutAdapter != null)
            runOnUiThread(() -> _episodeLayoutAdapter.notifyDataSetChanged());
    }

    public boolean isFirstIsLast() {
        return firstIsLast;
    }

    public void setFirstIsLast(boolean firstIsLast) {
        this.firstIsLast = firstIsLast;
    }
}


