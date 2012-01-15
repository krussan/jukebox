package se.qxx.android.jukebox;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ModelAdapter extends BaseAdapter {

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return Model.get().countMovies();
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return Model.get().getMovie(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
