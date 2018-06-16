package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class SeasonLayoutAdapter extends BaseAdapter {

	private Context context;
	private Series series;

	private Series getSeries() {
		return series;
	}
	public void setSeries(Series series) {
		this.series = series;
	}

	public SeasonLayoutAdapter(Context context, Series series) {
		super();
		this.context = context;
		this.series = series;
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

	@Override
	public int getCount() {
		return this.getSeries().getSeasonCount();
	}

	@Override
	public Object getItem(int position) {
		return this.getSeries().getSeason(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
	