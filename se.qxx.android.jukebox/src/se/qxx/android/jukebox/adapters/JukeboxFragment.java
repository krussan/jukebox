package se.qxx.android.jukebox.adapters;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.ActionDialog;
import se.qxx.android.jukebox.FlipperActivity;
import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.NowPlayingActivity;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.protobuf.InvalidProtocolBufferException;

public class JukeboxFragment extends ListFragment implements
	ModelUpdatedEventListener, OnItemClickListener, OnItemLongClickListener {
	private int position;
	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	private SeriesLayoutAdapter _seriesLayoutAdapter;
	
	private Runnable modelResultUpdatedRunnable = new Runnable() {

		@Override
		public void run() {
			_jukeboxMovieLayoutAdapter.notifyDataSetChanged();
			_seriesLayoutAdapter.notifyDataSetChanged();
		}
	};


	
	public static JukeboxFragment newInstance(int position) {
		Bundle b = new Bundle();
		JukeboxFragment mf = new JukeboxFragment();
		
		b.putInt("pos", position);
		mf.setArguments(b);
		
		return mf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		if (b != null) {
			this.position = b.getInt("position") ;
		}
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main, container, false);
		initializeView(v, this.position);
		return v;
	}
	
	private void initializeView(View v, int position) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//		JukeboxSettings.init(this);

		ListView lv = (ListView) v.findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		if (position == 0) {
			_jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(v.getContext());
			lv.setAdapter(_jukeboxMovieLayoutAdapter);
		}
		else {
			_seriesLayoutAdapter = new SeriesLayoutAdapter(v.getContext());
			lv.setAdapter(_seriesLayoutAdapter);
		}

		Model.get().addEventListener(this);

		
		//setupOnOffButton();

	    
	    //detector = new SimpleGestureFilter(this, this);
	}    

	public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, v.getContext());
		
		switch (id) {
			case R.id.btnPlay:
				Intent iPlay = new Intent(v.getContext(), NowPlayingActivity.class);
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		Model.get().setCurrentMovie(pos);
		Intent i = new Intent(arg1.getContext(), FlipperActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		ActionDialog d = new ActionDialog(this.getActivity(), Model.get().getMovie(arg2));
		d.show();
		return false;
	}

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent) e;

		if (ev.getType() == ModelUpdatedType.Movies) {
			this.getActivity().runOnUiThread(modelResultUpdatedRunnable);
		}
	}


	
}
