package se.qxx.android.jukebox.adapters.viewmode;

import java.util.EventObject;

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
		
	private int position;
    private String mode;

	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	private SeriesLayoutAdapter _seriesLayoutAdapter;
	private SeasonLayoutAdapter _seasonLayoutAdapter;
    private EpisodeLayoutAdapter _episodeLayoutAdapter;
    private EndlessScrollListener scrollListener;

	private Runnable modelResultUpdatedRunnable = new Runnable() {

		@Override
		public void run() {
			if (_jukeboxMovieLayoutAdapter != null)
				_jukeboxMovieLayoutAdapter.notifyDataSetChanged();
			
			if (_seriesLayoutAdapter != null)
				_seriesLayoutAdapter.notifyDataSetChanged();

            if (_seasonLayoutAdapter != null)
                _seasonLayoutAdapter.notifyDataSetChanged();

            if (_episodeLayoutAdapter != null)
                _episodeLayoutAdapter.notifyDataSetChanged();
		}
	};
	
	public static JukeboxFragment newInstance(int position, String mode) {
		Bundle b = new Bundle();
		JukeboxFragment mf = new JukeboxFragment();
		
		b.putInt("position", position);
        b.putString("mode", mode);
		mf.setArguments(b);

		return mf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		if (b != null) {
			this.position = b.getInt("position");
            this.mode = b.getString("mode");
		}

        Model.get().addEventListener(this);
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main, container, false);
		initializeView(v, this.position);
		return v;
	}
	
	private void initializeView(View v, int position) {
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

        if (StringUtils.equalsIgnoreCase(this.mode, "main")) {
            if (position == 0) {
                _jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(v.getContext());
                lv.setAdapter(_jukeboxMovieLayoutAdapter);
            }
            else {
                _seriesLayoutAdapter = new SeriesLayoutAdapter(v.getContext());
                lv.setAdapter(_seriesLayoutAdapter);
            }
        }

        if (StringUtils.equalsIgnoreCase(this.mode, "season")) {
            _seasonLayoutAdapter = new SeasonLayoutAdapter(v.getContext(), Model.get().getCurrentSeries());
            lv.setAdapter(_seasonLayoutAdapter);
        }

        if (StringUtils.equalsIgnoreCase(this.mode, "episode")) {
            _episodeLayoutAdapter = new EpisodeLayoutAdapter(v.getContext(), Model.get().getCurrentSeason());
            lv.setAdapter(_episodeLayoutAdapter);
        }



		Connector.setupOnOffButton(v);

	    
	    //detector = new SimpleGestureFilter(this, this);
	}

	private void loadMoreData(int offset) {
        Connector.connect(offset, Model.get().getNrOfItems());
    }


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (StringUtils.equalsIgnoreCase(this.mode, "main")) {
            if (this.position == 0) {
                Model.get().setCurrentMovie(pos);

                Intent i = new Intent(arg1.getContext(), FlipperActivity.class);
                i.putExtra("mode", "main");
                startActivity(i);
            } else {
                Model.get().setCurrentSeries(pos);
                Intent intentSeries = new Intent(this.getActivity(), FlipperListActivity.class);
                intentSeries.putExtra("mode", "Season");

                startActivity(intentSeries);

                //Toast.makeText(this.getActivity(), "Should display the new series!", Toast.LENGTH_SHORT).show();
            }
        }

        if (StringUtils.equalsIgnoreCase(this.mode, "season")) {
            Model.get().setCurrentSeason(pos);
            Intent intentSeries = new Intent(this.getActivity(), FlipperListActivity.class);
            intentSeries.putExtra("mode", "episode");

            startActivity(intentSeries);
        }

        if (StringUtils.equalsIgnoreCase(this.mode, "epsiode")) {
            Model.get().setCurrentEpisode(pos);

            Intent i = new Intent(arg1.getContext(), FlipperActivity.class);
            i.putExtra("mode", "episode");
            startActivity(i);
        }
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		
		ActionDialog d = null;
		
		switch (this.position){
		case 0:
			d = new ActionDialog(this.getActivity(), Model.get().getMovie(arg2).getID(), RequestType.TypeMovie);
			break;
		case 1:
			d = new ActionDialog(this.getActivity(), Model.get().getSeries(arg2).getID(), RequestType.TypeSeries);
		}
		
		if (d != null)
			d.show();
		
		return false;
	}

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent) e;

		if (ev.getType() == ModelUpdatedType.Movies || ev.getType() == ModelUpdatedType.Series) {
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

			Connector.connect(Model.get().getOffset(), Model.get().getNrOfItems());
			
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
