package se.qxx.android.jukebox.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public abstract class ModelSeasonAdapter extends BaseAdapter {

	private Series _series;

	private Series getSeries() {
		return _series;
	}

	public ModelSeasonAdapter(Series series) {
		super();

		this._series = series;
	}
	
	@Override
	public int getCount() {
		return this.getSeries().getSeasonCount();
	}
	
	@Override
	public Object getItem(int position) {
		return this.getSeries().getSeason(position);
	}
	
	@Override
	public long getItemId(int position) {		
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
