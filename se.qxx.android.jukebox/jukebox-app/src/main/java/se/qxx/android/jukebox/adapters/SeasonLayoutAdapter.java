package se.qxx.android.jukebox.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.ModelSeriesAdapter;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Season;

public class SeasonLayoutAdapter extends ModelSeriesAdapter {

	private Context context;
	public SeasonLayoutAdapter(Context context) {
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
			Context context = v.getContext();

			Season ss = (Season)this.getItem(position);
	        if (ss != null) {
	        	GUITools.setTextOnTextview(R.id.toptext, String.format("Season %s - %s", ss.getSeasonNumber(), ss.getTitle()), v);
	        	GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(ss.getYear()), v);
//	        	GUITools.setTextOnTextview(R.id.txtRating, m.getRating(), v);
	        	
	        	// If all media has a meta duration then hide the download icon
        		GUITools.hideView(R.id.imgDownloading, v);
	        		
	    	    if (ss.getThumbnail().isEmpty()) {
					GUITools.setImageResourceOnImageView(R.id.imageView1, R.drawable.icon, v);
	    	    }
	    	    else {
					Bitmap image = GUITools.getBitmapFromByteArray(ss.getThumbnail().toByteArray());
					Bitmap scaledImage = GUITools.scaleImage(80, image, v.getContext());
					GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
	    	    }
	    	}
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
			
        return v;
	}
}
	