package se.qxx.android.jukebox.model;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ModelMediaAdapter extends BaseAdapter {

	Movie movie = null;
	
	public ModelMediaAdapter(Movie movie) {
		this.movie = movie;
	}
	
	@Override
	public int getCount() {
		return this.movie.getMediaCount();
	}
	
	@Override
	public Object getItem(int position) {
		return this.movie.getMedia(position);
	}
	
	@Override
	public long getItemId(int position) {
		return this.movie.getMedia(position).getID();
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
