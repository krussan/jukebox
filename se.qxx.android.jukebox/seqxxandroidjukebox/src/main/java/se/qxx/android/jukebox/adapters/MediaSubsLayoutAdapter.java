package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.IncludeSubtitleRating;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.ModelRatedSubtitleAdapter;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MediaSubsLayoutAdapter extends ModelRatedSubtitleAdapter  {

	private Context context;
	public MediaSubsLayoutAdapter(Context context, Media media) {
		super(media);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView; 

		try {
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.subtitleitem, null);
	        }
	        Subtitle sub = (Subtitle)this.getItem(position);
	        
	        IncludeSubtitleRating.initialize(sub, v);
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating subtitle list", e);
		}
			
        return v;
	}
}
	