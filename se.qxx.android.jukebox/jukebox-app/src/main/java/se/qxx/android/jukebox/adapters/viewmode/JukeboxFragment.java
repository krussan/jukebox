package se.qxx.android.jukebox.adapters.viewmode;

import java.util.EventObject;

import se.qxx.android.jukebox.activities.ListActivity;
import se.qxx.android.jukebox.activities.ViewMode;
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
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
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
	ModelUpdatedEventListener, OnItemClickListener, OnItemLongClickListener, OnClickListener {

    private ViewMode mode;

	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	private SeriesLayoutAdapter _seriesLayoutAdapter;
    private EndlessScrollListener scrollListener;

    public ViewMode getMode() {
        return mode;
    }

    public void setMode(ViewMode mode) {
        this.mode = mode;
    }


	private Runnable modelResultUpdatedRunnable = new Runnable() {

		@Override
		public void run() {
			if (_jukeboxMovieLayoutAdapter != null)
				_jukeboxMovieLayoutAdapter.notifyDataSetChanged();
			
			if (_seriesLayoutAdapter != null)
				_seriesLayoutAdapter.notifyDataSetChanged();

		}
	};

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
		Bundle b = getArguments();

		if (b != null) {
            this.setMode((ViewMode)b.getSerializable("mode"));
		}

        Model.get().addEventListener(this);
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

		if (this.getMode() == ViewMode.Movie) {
            _jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(v.getContext());
            lv.setAdapter(_jukeboxMovieLayoutAdapter);
        }
        else if (this.getMode() == ViewMode.Series) {
            _seriesLayoutAdapter = new SeriesLayoutAdapter(v.getContext());
            lv.setAdapter(_seriesLayoutAdapter);
        }

		Connector.setupOnOffButton(v);

	    
	    //detector = new SimpleGestureFilter(this, this);
	}

	private void loadMoreData(int offset) {
        Connector.connect(offset, Model.get().getNrOfItems(), ViewMode.getModelType(this.getMode()));
    }


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (this.getMode() == ViewMode.Movie) {
            Model.get().setCurrentMovie(pos);

            Intent i = new Intent(arg1.getContext(), FlipperActivity.class);
            i.putExtra("mode", ViewMode.Movie);
            startActivity(i);
        }
        else if (this.getMode() == ViewMode.Series) {
            Model.get().setCurrentSeries(pos);
            Intent intentSeries = new Intent(this.getActivity(), ListActivity.class);
            intentSeries.putExtra("mode", ViewMode.Season);

            startActivity(intentSeries);

            //Toast.makeText(this.getActivity(), "Should display the new series!", Toast.LENGTH_SHORT).show();
        }
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		
		ActionDialog d = null;

        if (this.getMode() == ViewMode.Movie) {
            d = new ActionDialog(this.getActivity(), Model.get().getMovie(arg2).getID(), RequestType.TypeMovie);
        }
        else if (this.getMode() == ViewMode.Series) {
            d = new ActionDialog(this.getActivity(), Model.get().getSeries(arg2).getID(), RequestType.TypeSeries);
        }

		if (d != null)
			d.show();
		
		return false;
	}

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent) e;

		if (ev.getType() == ModelUpdatedType.Movies || ev.getType() == ModelUpdatedType.Series || ev.getType() == ModelUpdatedType.Season) {
			Activity a = this.getActivity();

			if (a != null)
				a.runOnUiThread(modelResultUpdatedRunnable);
		}
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

	

	
}
