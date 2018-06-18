package se.qxx.android.jukebox.adapters.viewmode;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import se.qxx.android.jukebox.activities.ListActivity;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.adapters.support.IOffsetHandler;
import se.qxx.android.jukebox.dialogs.ActionDialog;
import se.qxx.android.jukebox.comm.Connector;
import se.qxx.android.jukebox.adapters.support.EndlessScrollListener;
import se.qxx.android.jukebox.activities.FlipperActivity;
import se.qxx.android.jukebox.activities.FlipperListActivity;
import se.qxx.android.jukebox.activities.JukeboxPreferenceActivity;
import se.qxx.android.jukebox.activities.PlayerPickerActivity;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.list.EpisodeLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.MovieLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeasonLayoutAdapter;
import se.qxx.android.jukebox.adapters.list.SeriesLayoutAdapter;
import se.qxx.android.jukebox.model.Constants;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import org.apache.commons.lang3.StringUtils;

public class JukeboxFragment extends ListFragment implements
	OnItemClickListener, OnItemLongClickListener, OnClickListener, Connector.ConnectorCallbackEventListener, IOffsetHandler {

    private ViewMode mode;

	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	private SeriesLayoutAdapter _seriesLayoutAdapter;
    private EndlessScrollListener scrollListener;
    private int offset;

    public ViewMode getMode() {
        return mode;
    }

    @Override
    public JukeboxDomain.Season getSeason() {
        return null;
    }

    @Override
    public JukeboxDomain.Series getSeries() {
        return null;
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

	private static ViewMode getViewMode(int position) {
        // position 0 in horizontal scroll is movie
        // position 0 is series OR season

        if (position == 0)
            return ViewMode.Movie;
        else
            return ViewMode.Series;
    }

	public static JukeboxFragment newInstance(int position) {
		Bundle b = new Bundle();
		JukeboxFragment mf = new JukeboxFragment();
        b.putSerializable("mode", getViewMode(position));

		mf.setArguments(b);

		return mf;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(false);

        Bundle b = getArguments();

        if (b != null) {
            this.setMode((ViewMode) b.getSerializable("mode"));
        }

        Connector.addEventListener(this);

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
        if (getUserVisibleHint()) {
            clearData();
            loadMoreData(0);
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main, container, false);
		initializeView(v);
		return v;
	}
	
	private void initializeView(View v) {
		ListView lv = (ListView) v.findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		v.findViewById(R.id.btnRefresh).setOnClickListener(this);
		v.findViewById(R.id.btnSelectMediaPlayer).setOnClickListener(this);
		v.findViewById(R.id.btnCurrentMovie).setOnClickListener(this);
		v.findViewById(R.id.btnPreferences).setOnClickListener(this);
		v.findViewById(R.id.btnOn).setOnClickListener(this);
		v.findViewById(R.id.btnOff).setOnClickListener(this);

		scrollListener = new EndlessScrollListener(this) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (!Model.get().isLoading()) {
                    Model.get().setLoading(true);

                    this.getHandler().setOffset(page * Constants.NR_OF_ITEMS);

                    loadMoreData(page * Constants.NR_OF_ITEMS);
                }

                return true;
            }
        };

		lv.setOnScrollListener(scrollListener);

		if (this.getMode() == ViewMode.Movie) {
            _jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(v.getContext(), new ArrayList<JukeboxDomain.Movie>());
            lv.setAdapter(_jukeboxMovieLayoutAdapter);
        }
        else if (this.getMode() == ViewMode.Series) {
            _seriesLayoutAdapter = new SeriesLayoutAdapter(v.getContext(), new ArrayList<JukeboxDomain.Series>());
            lv.setAdapter(_seriesLayoutAdapter);
        }

		Connector.setupOnOffButton(v);

	    
	    //detector = new SimpleGestureFilter(this, this);
	}

	private void loadMoreData(int offset) {
        Connector.connect(
                offset,
                Constants.NR_OF_ITEMS,
                ViewMode.getModelType(this.getMode()),
                -1,
                -1);
    }


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (this.getMode() == ViewMode.Movie) {
            Intent i = new Intent(arg1.getContext(), FlipperActivity.class);
            i.putExtra("mode", ViewMode.Movie);
            i.putExtra("position", pos);
            i.putExtra("movies", (ArrayList<JukeboxDomain.Movie>)_jukeboxMovieLayoutAdapter.getMovies());

            startActivity(i);
        }
        else if (this.getMode() == ViewMode.Series) {
            Intent intentSeries = new Intent(this.getActivity(), ListActivity.class);
            intentSeries.putExtra("mode", ViewMode.Season);
            intentSeries.putExtra("series", (JukeboxDomain.Series)_seriesLayoutAdapter.getItem(pos));

            startActivity(intentSeries);
        }
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		
		ActionDialog d = null;

        if (this.getMode() == ViewMode.Movie) {
            d = new ActionDialog(
                    this.getActivity(),
                    _jukeboxMovieLayoutAdapter.getItemId(pos),
                    RequestType.TypeMovie);
        }
        else if (this.getMode() == ViewMode.Series) {
            d = new ActionDialog(
                    this.getActivity(),
                    _seriesLayoutAdapter.getItemId(pos),
                    RequestType.TypeSeries);
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
			loadMoreData(0);

			break;
		case R.id.btnSelectMediaPlayer:
			Logger.Log().i("selectMediaPlayerClicked");

			Intent i = new Intent(this.getActivity(), PlayerPickerActivity.class);
			startActivity(i);
			break;
		case R.id.btnOn:
		case R.id.btnOff:
			Connector.onoff(this.getActivity());
			Connector.setupOnOffButton(this.getView());
			break;
		case R.id.btnPreferences:
			Intent intentPreferences = new Intent(this.getActivity(), JukeboxPreferenceActivity.class);
			startActivity(intentPreferences);
			break;
		default:
			break;

		}	
	}


    @Override
    public void handleMoviesUpdated(List<JukeboxDomain.Movie> movies) {
	    if (_jukeboxMovieLayoutAdapter != null) {
            _jukeboxMovieLayoutAdapter.addMovies(movies);
            notifyMovieList();
        }
    }

    public void notifyMovieList() {
        if (_jukeboxMovieLayoutAdapter != null)
            this.getActivity().runOnUiThread(() -> _jukeboxMovieLayoutAdapter.notifyDataSetChanged());

    }

    public void notifySeriesList() {
        if (_seriesLayoutAdapter != null)
            this.getActivity().runOnUiThread(() -> _seriesLayoutAdapter.notifyDataSetChanged());

    }

    @Override
    public void handleSeriesUpdated(List<JukeboxDomain.Series> series) {
        if (_seriesLayoutAdapter != null) {
            _seriesLayoutAdapter.addSeries(series);
            notifySeriesList();
        }
    }

    @Override
    public void handleSeasonsUpdated(List<JukeboxDomain.Season> seasons) {

    }

    @Override
    public void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes) {

    }


    private void clearData() {
        this.setOffset(0);
	    if (this.getMode() == ViewMode.Movie && _jukeboxMovieLayoutAdapter != null) {
            _jukeboxMovieLayoutAdapter.clearMovies();
            notifyMovieList();
        }
        else if (this.getMode() == ViewMode.Series && _seriesLayoutAdapter != null) {
	        _seriesLayoutAdapter.clearSeries();
	        notifySeriesList();
        }
    }

}
