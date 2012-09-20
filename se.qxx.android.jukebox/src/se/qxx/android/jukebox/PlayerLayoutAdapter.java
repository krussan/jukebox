package se.qxx.android.jukebox;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.R.drawable;
import se.qxx.android.jukebox.R.id;
import se.qxx.android.jukebox.R.layout;
import se.qxx.android.jukebox.model.ModelMovieAdapter;
import se.qxx.android.jukebox.model.ModelPlayersAdapter;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class PlayerLayoutAdapter extends ArrayAdapter<String>{

	private Context context;
	public PlayerLayoutAdapter(Context context, int layoutResource, int textViewResorce, List<String> values) {
		super(context, layoutResource, textViewResorce, values);
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
	        String name = this.getItem(position);
	        String currentMediaPlayer = JukeboxSettings.get().getCurrentMediaPlayer();
	        
	        GUITools.setTextOnTextview(R.id.txtPlayerName, currentMediaPlayer, v);
	        
	        if (name.equals(currentMediaPlayer))
	        	GUITools.setImageResourceOnImageButton(R.id.btnPickPlayer, R.drawable.selected, v);
	        
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
			
        return v;
	}
}
	