package se.qxx.android.jukebox.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ModelPlayersAdapter extends BaseAdapter {

	@Override
	public int getCount() {
		return Model.get().countPlayers();
	}
	
	@Override
	public Object getItem(int position) {
		
		return Model.get().getPlayer(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	

}
