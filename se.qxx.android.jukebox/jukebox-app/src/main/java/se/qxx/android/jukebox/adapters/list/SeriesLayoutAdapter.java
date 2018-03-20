package se.qxx.android.jukebox.adapters.list;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SeriesLayoutAdapter extends BaseAdapter {

	private Context context;

	public SeriesLayoutAdapter(Context context) {
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

			Series m = (Series)this.getItem(position);
	        if (m != null) {
	        	GUITools.setTextOnTextview(R.id.toptext, m.getTitle(), v);
	        	GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(m.getYear()), v);
//	        	GUITools.setTextOnTextview(R.id.txtRating, m.getRating(), v);
	        	
	        	// If all media has a meta duration then hide the download icon
        		GUITools.hideView(R.id.imgDownloading, v);
	        		
	    	    if (!m.getThumbnail().isEmpty()) {
	    	    	Bitmap image = GUITools.getBitmapFromByteArray(m.getThumbnail().toByteArray());
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

}
	