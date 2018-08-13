package se.qxx.android.jukebox.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ModelSeriesAdapter extends BaseAdapter {

	public ModelSeriesAdapter() {
		super();
	}
	
	@Override
	public int getCount() {
		return Model.get().countSeries();
	}
	
	@Override
	public Object getItem(int position) {
		
		return Model.get().getSeries(position);
	}
	
	@Override
	public long getItemId(int position) {		
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
