package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class SeasonLayoutAdapter extends BaseAdapter {

	private Context context;
	private List<Season> seasons = new ArrayList<>();

    public List<Season> getSeasons() {
        return seasons;
    }

    public void addSeasons(List<Season> seasons) {
        this.getSeasons().addAll(seasons);
    }

    public void clearSeasons() {
        this.getSeasons().clear();
    }

	public SeasonLayoutAdapter(Context context, Series series) {
		super();
		this.context = context;
		this.clearSeasons();
		this.addSeasons(series.getSeasonList());
	}

    public SeasonLayoutAdapter(Context context, List<Season> seasns) {
        super();
        this.context = context;
        this.clearSeasons();
        this.addSeasons(seasons);
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
		return this.getSeasons().size();
	}

	@Override
	public Object getItem(int position) {
		return this.getSeasons().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}



}
	