package se.qxx.android.jukebox.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ModelMovieAdapter extends BaseAdapter {

	@Override
	public int getCount() {
		return Model.get().countMovies();
	}
	
	@Override
	public Object getItem(int position) {
		
		return Model.get().getMovie(position);
	}
	
	@Override
	public long getItemId(int position) {
		
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
