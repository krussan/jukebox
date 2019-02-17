package se.qxx.android.jukebox.adapters.support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;

public class PlayerLayoutAdapter extends BaseAdapter {

	private Context context;
	private List<String> players = new ArrayList<String>();
	private JukeboxSettings settings;

	public PlayerLayoutAdapter(Context context) {
		super();
		this.context = context;
		settings = new JukeboxSettings(context);
	}

	public void addPlayer(String player) {
	    this.players.add(player);
    }

	public void addPlayers(List<String> players) {
		this.players.addAll(players);
	}

	public void clarPlayers() {
		this.players.clear();
	}

	public List<String> getPlayers() {
	    return this.players;
    }

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView; 

		try {
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.playerpickerrow, null);
	        }

			String name = (String)this.getItem(position);
	        String currentMediaPlayer = settings.getCurrentMediaPlayer();
	        
	        GUITools.setTextOnTextview(R.id.txtPlayerName, name, v);
	        
	        if (name.equals(currentMediaPlayer))
	        	GUITools.setImageResourceOnImageView(R.id.imgPlayerSelected, R.drawable.selected, v);
	        else
	        	GUITools.setImageResourceOnImageView(R.id.imgPlayerSelected, R.drawable.todo, v);
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
			
        return v;
	}

	@Override
	public int getCount() {
		return this.getPlayers().size();
	}

	@Override
	public Object getItem(int position) {

		return this.getPlayers().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


}
	