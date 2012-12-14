package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.R.drawable;
import se.qxx.android.jukebox.R.id;
import se.qxx.android.jukebox.R.layout;
import se.qxx.android.jukebox.model.ModelMovieAdapter;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MovieLayoutAdapter extends ModelMovieAdapter {

	private Context context;
	public MovieLayoutAdapter(Context context) {
		super();
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView; 
		
		try {
			
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.movielistrow, null);
	        }
	        Movie m = (Movie)this.getItem(position);
	        if (m != null) {
	        	GUITools.setTextOnTextview(R.id.toptext, m.getTitle(), v);
	        	GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(m.getYear()), v);
	        	GUITools.setTextOnTextview(R.id.txtRating, m.getRating(), v);
	        	
	        	//TODO: MEDIA -- Fix This
//	        	if (m.getMetaDuration() >= 0)
//	        		GUITools.hideView(R.id.imgDownloading, v);
	
	    	    if (!m.getImage().isEmpty()) {
	    	    	Bitmap image = GUITools.getBitmapFromByteArray(m.getImage().toByteArray());
	    	    	Bitmap scaledImage = GUITools.scaleImage(80, image, v.getContext());
	    	    	GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
	    	    }
	    	    else {
	    	    	GUITools.setImageResourceOnImageView(R.id.imageView1, R.drawable.icon, v);
	    	    }
	    	}
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
			
        return v;
	}
}
	