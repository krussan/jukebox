package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.ModelPlayersAdapter;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlayerLayoutAdapter extends ModelPlayersAdapter {

	private Context context;
	public PlayerLayoutAdapter(Context context) {
		super();
		this.context = context;
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
	        String currentMediaPlayer = JukeboxSettings.get().getCurrentMediaPlayer();
	        
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
}
	