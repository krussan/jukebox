package se.qxx.android.jukebox.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import se.qxx.jukebox.domain.JukeboxDomain.Season;

public abstract class ModelEpisodeAdapter extends BaseAdapter {

	private Season _season;

	protected Season getSeason() {
		return _season;
	}

	public ModelEpisodeAdapter(Season season) {
		super();

		this._season = season;
	}
	
	@Override
	public int getCount() {
		return this.getSeason().getEpisodeCount();
	}
	
	@Override
	public Object getItem(int position) {
		return this.getSeason().getEpisode(position);
	}
	
	@Override
	public long getItemId(int position) {		
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
