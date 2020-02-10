package se.qxx.android.jukebox.adapters.viewmode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ListActivity;
import se.qxx.android.jukebox.activities.MovieDetailActivity;
import se.qxx.android.jukebox.activities.NowPlayingActivity;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.adapters.list.EpisodeLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.MovieLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeasonLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeriesLayoutAdapter;
import se.qxx.android.jukebox.adapters.support.EndlessScrollListener;
import se.qxx.android.jukebox.adapters.support.IOffsetHandler;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.dialogs.ActionDialog;
import se.qxx.android.jukebox.model.Constants;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;

import static se.qxx.android.jukebox.model.Constants.NR_OF_ITEMS;

public class JukeboxFragment extends ListFragment implements
	OnItemClickListener, OnItemLongClickListener, JukeboxConnectionHandler.ConnectorCallbackEventListener, IOffsetHandler, SwipeRefreshLayout.OnRefreshListener {

	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	private SeriesLayoutAdapter _seriesLayoutAdapter;
    private SeasonLayoutAdapter _seasonLayoutAdapter;
    private EpisodeLayoutAdapter _episodeLayoutAdapter;

    private EndlessScrollListener scrollListener;
    private int offset;
    private int totalItems;
    private boolean isLoading;
    private JukeboxSettings settings;
    private JukeboxFragmentHandler handler;
    private SwipeRefreshLayout swipeLayout;
    private String searchString = "";
    private boolean firstIsLast = false;

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public ViewMode getMode() {
        Bundle b = getArguments();

        if (b != null) {
            return ((ViewMode) b.getSerializable("mode"));
        }

        return ViewMode.Movie;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public JukeboxDomain.Series getSeries() {
        Bundle b = getArguments();
        if (b != null)
            return (JukeboxDomain.Series) b.getSerializable("series");

        return null;
    }

    public JukeboxDomain.Season getSeason() {

        Bundle b = getArguments();
        if (b != null)
            return (JukeboxDomain.Season) b.getSerializable("season");

        return null;
    }

    public int getSeriesID() {
        ViewMode mode = this.getMode();

        if ((mode == ViewMode.Season || mode == ViewMode.Episode) && this.getSeries() != null)
            return this.getSeries().getID();
        else
            return -1;
    }

    public int getSeasonID() {
        ViewMode mode = this.getMode();

        if ((mode == ViewMode.Season || mode == ViewMode.Episode) && this.getSeason() != null)
            return this.getSeason().getID();
        else
            return -1;
    }

    private boolean isFirstIsLast() {
        return firstIsLast;
    }

    public void setFirstIsLast(boolean firstIsLast) {
        this.firstIsLast = firstIsLast;
    }

    private static JukeboxFragment createFragment(Bundle b) {
        JukeboxFragment mf = new JukeboxFragment();
        mf.setArguments(b);

        return mf;
    }

    public static JukeboxFragment newInstance(ViewMode mode) {
        return newInstance(mode, null, null);
    }

    public static JukeboxFragment newInstance(ViewMode mode, JukeboxDomain.Series series, JukeboxDomain.Season season) {
		Bundle b = new Bundle();
        b.putSerializable("mode", mode);
		if  (mode == ViewMode.Season) {
            b.putSerializable("series", series);
        } else if (mode == ViewMode.Episode)  {
            b.putSerializable("series", series);
            b.putSerializable("season", season);
        }

        return createFragment(b);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(false);

        settings = new JukeboxSettings(this.getContext());

        clearData();
        Logger.Log().d("Initializing - loading data");
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main, container, false);
		initializeView(v);
		return v;
	}

	private void initializeView(View v) {
		ListView lv = v.findViewById(R.id.listItems);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

        swipeLayout = v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

		v.findViewById(R.id.txtListTitle).setVisibility(View.GONE);

		scrollListener = new EndlessScrollListener(this) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount >= getTotalItems())
                    return false;

                if (!isLoading && !isFirstIsLast()) {
                    this.getHandler().setOffset(page * Constants.NR_OF_ITEMS);

                    Logger.Log().d("EndlessScroll event - Loading more data");

                    if (getMode() == ViewMode.Episode)
                        return false;

                    loadMoreData(page * Constants.NR_OF_ITEMS);
                }

                return true;
            }
        };

		lv.setOnScrollListener(scrollListener);

		lv.setAdapter(getLayoutAdapter(v));
        loadMoreData(0);
	}

    private ListAdapter getLayoutAdapter(View v) {
        ViewMode mode = this.getMode();
        if (mode == ViewMode.Movie) {
            _jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(v.getContext(), new ArrayList<>());
            return _jukeboxMovieLayoutAdapter;
        }
        else if (mode == ViewMode.Series) {
            _seriesLayoutAdapter = new SeriesLayoutAdapter(v.getContext(), new ArrayList<>());
            return _seriesLayoutAdapter;
        }
        else if (mode == ViewMode.Season) {
            _seasonLayoutAdapter = new SeasonLayoutAdapter(v.getContext(), this.getSeries().getSeasonList());
            GUITools.setTextOnTextview(R.id.txtListTitle, this.getSeries().getTitle(), v);
            return _seasonLayoutAdapter;
        }
        else if (mode == ViewMode.Episode) {
            _episodeLayoutAdapter = new EpisodeLayoutAdapter(v.getContext(),
                            this.getSeason().getSeasonNumber(),
                            this.getSeason().getEpisodeList());

            GUITools.setTextOnTextview(R.id.txtListTitle,
                    String.format("%s - Season %s",
                            this.getSeries().getTitle(),
                            this.getSeason().getSeasonNumber()), v);
            return _episodeLayoutAdapter;
        }

        return null;
    }

	private void loadMoreData(int offset) {
        setLoading(true);
        if (this.handler != null)
            this.handler.getConnectionHandler().connect(
                    this.getSearchString(),
                    offset,
                    Constants.NR_OF_ITEMS,
                    this.getMode(),
                    this.getSeriesID(),
                    this.getSeasonID(),
                    true,
                    true);
    }


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (this.getMode() == ViewMode.Movie) {
            Intent i = new Intent(arg1.getContext(), MovieDetailActivity.class);
            i.putExtra("mode", ViewMode.Movie);
            i.putExtra("movie", _jukeboxMovieLayoutAdapter.getItem(pos));

            startActivity(i);
        }
        else if (this.getMode() == ViewMode.Series) {
            this.handler.switchFragment(ViewMode.Season,
                    _seriesLayoutAdapter.getItem(pos),
                    null);
        }
        else if (this.getMode() == ViewMode.Season) {
            this.handler.switchFragment(ViewMode.Episode,
                    this.getSeries(),
                    _seasonLayoutAdapter.getItem(pos));
        }
        else if (this.getMode() == ViewMode.Episode) {
            JukeboxDomain.Episode e = _episodeLayoutAdapter.getItem(pos);

            if (e != null) {
                Intent iPlay = new Intent(this.getActivity(), NowPlayingActivity.class);
                iPlay.putExtra("mode", ViewMode.Episode);
                iPlay.putExtra("ID", e.getID());
                iPlay.putExtra("seasonNumber", this.getSeason().getSeasonNumber());

                startActivity(iPlay);
            }
        }
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {

		ActionDialog d = null;

        if (this.getMode() == ViewMode.Movie) {
            JukeboxDomain.Movie m = _jukeboxMovieLayoutAdapter.getItem(pos);
            d = new ActionDialog(
                    this.getActivity(),
                    _jukeboxMovieLayoutAdapter.getItemId(pos),
                    _jukeboxMovieLayoutAdapter.getMedia(pos),
                    RequestType.TypeMovie,
                    this.handler.getConnectionHandler());
        }
        else if (this.getMode() == ViewMode.Series) {
            d = new ActionDialog(
                    this.getActivity(),
                    _seriesLayoutAdapter.getItemId(pos),
                    null,
                    RequestType.TypeSeries,
                    this.handler.getConnectionHandler());
        }
        else if (this.getMode() == ViewMode.Season) {
            JukeboxDomain.Season ss = _seasonLayoutAdapter.getItem(pos);

            d = new ActionDialog(
                    this.getActivity(),
                    ss.getID(),
                    null,
                    JukeboxDomain.RequestType.TypeSeason,
                    this.handler.getConnectionHandler());
        }
        else if (this.getMode() == ViewMode.Episode) {
            JukeboxDomain.Episode e = _episodeLayoutAdapter.getItem(pos);

            d = new ActionDialog(
                    this.getActivity(),
                    e.getID(),
                    e.getMediaCount() > 0 ? e.getMedia(0) : null,
                    JukeboxDomain.RequestType.TypeEpisode,
                    this.handler.getConnectionHandler());
        }

		if (d != null)
			d.show();

		return false;
	}

    @Override
    public void handleMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies) {
        swipeLayout.setRefreshing(false);
	    if (_jukeboxMovieLayoutAdapter != null) {
            setLoading(false);
            this.setTotalItems(totalMovies);

            _jukeboxMovieLayoutAdapter.addMovies(movies);
            _jukeboxMovieLayoutAdapter.setServerListSize(totalMovies);
            notifyMovieList();
        }
    }

    private void notifyMovieList() {
        if (_jukeboxMovieLayoutAdapter != null && this.getActivity() != null)
            this.getActivity().runOnUiThread(() -> _jukeboxMovieLayoutAdapter.notifyDataSetChanged());
    }

    private void notifySeriesList() {
        if (_seriesLayoutAdapter != null && this.getActivity() != null)
            this.getActivity().runOnUiThread(() -> _seriesLayoutAdapter.notifyDataSetChanged());
    }

    private void notifySeasons() {
        if (_seasonLayoutAdapter != null && this.getActivity() != null)
            this.getActivity().runOnUiThread(() -> _seasonLayoutAdapter.notifyDataSetChanged());
    }

    private void notifyEpisodes() {
        if (_episodeLayoutAdapter != null && this.getActivity() != null)
            this.getActivity().runOnUiThread(() -> _episodeLayoutAdapter.notifyDataSetChanged());
    }


    @Override
    public void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries) {
        swipeLayout.setRefreshing(false);
        if (_seriesLayoutAdapter != null) {
            setLoading(false);
            this.setTotalItems(totalSeries);
            _seriesLayoutAdapter.addSeries(series);
            _seriesLayoutAdapter.setServerListSize(totalSeries);
            notifySeriesList();
        }
    }

    @Override
    public void handleSeasonsUpdated(List<JukeboxDomain.Season> seasons, int totalSeasons) {
        setLoading(false);
        if (this.getMode() == ViewMode.Season) {
            this.setTotalItems(totalSeasons);
            if (_seasonLayoutAdapter != null) {
                _seasonLayoutAdapter.addSeasons(seasons);

                if (this.getOffset() == 0 && seasons.size() <= NR_OF_ITEMS)
                    this.setFirstIsLast(true);

                notifySeasons();
            }
        }

    }

    @Override
    public void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes, int totalEpisodes) {
        setLoading(false);
        if (this.getMode() == ViewMode.Episode) {
            this.setTotalItems(totalEpisodes);
            if (_episodeLayoutAdapter != null) {
                _episodeLayoutAdapter.addEpisodes(episodes);

                if (this.getOffset() == 0 && episodes.size() <= NR_OF_ITEMS)
                    this.setFirstIsLast(true);

                notifyEpisodes();
            }
        }
    }


    private void clearData() {
        this.setOffset(0);
        ViewMode mode = this.getMode();
	    if (mode == ViewMode.Movie && _jukeboxMovieLayoutAdapter != null) {
            _jukeboxMovieLayoutAdapter.clearMovies();
            notifyMovieList();
        }
        else if (mode == ViewMode.Series && _seriesLayoutAdapter != null) {
	        _seriesLayoutAdapter.clearSeries();
	        notifySeriesList();
        }
        else if (mode == ViewMode.Season && _seasonLayoutAdapter != null) {
            _seasonLayoutAdapter.clearSeasons();
            notifySeasons();
        }
        else if (mode == ViewMode.Episode && _episodeLayoutAdapter != null) {
            _episodeLayoutAdapter.clearEpisodes();
            notifyEpisodes();
        }
    }

    private void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
        if (_jukeboxMovieLayoutAdapter != null)
            _jukeboxMovieLayoutAdapter.setLoading(isLoading);

        if (_seriesLayoutAdapter != null)
            _seriesLayoutAdapter.setLoading(isLoading);
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        clearData();
        loadMoreData(0);
    }

    public interface JukeboxFragmentHandler {
        JukeboxConnectionHandler getConnectionHandler();
        void switchFragment(ViewMode newMode, JukeboxDomain.Series series, JukeboxDomain.Season season);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof JukeboxFragmentHandler) {
            handler = (JukeboxFragmentHandler) context;
            handler.getConnectionHandler().addCallback(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement JukeboxFragmentHandler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handler.getConnectionHandler().removeCallback(this);
    }

    public boolean onSearch(String searchString) {
        this.clearData();
        this.setSearchString(searchString);

        loadMoreData(0);
        return true;
    }


}
