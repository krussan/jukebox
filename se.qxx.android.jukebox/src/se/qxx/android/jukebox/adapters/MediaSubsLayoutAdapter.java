package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.ModelRatedSubtitleAdapter;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
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
	        
	        if (sub != null) {
	        	GUITools.setTextOnTextview(R.id.txtSubName, sub.getDescription(), v);
	        	Rating rating = sub.getRating();
	        	switch (rating) {
	        	case SubsExist:
	    	        GUITools.showView(R.id.imgSubRatingExact, v);
	    	        GUITools.showView(R.id.imgSubRatingPositive, v);
	    	        GUITools.showView(R.id.imgSubRatingProbable, v);
	    	        GUITools.showView(R.id.imgSubRatingSubsExist, v);
	    	        break;	        	
	        	case ExactMatch:
	    	        GUITools.showView(R.id.imgSubRatingExact, v);
	    	        GUITools.showView(R.id.imgSubRatingPositive, v);
	    	        GUITools.showView(R.id.imgSubRatingProbable, v);
	    	        GUITools.hideView(R.id.imgSubRatingSubsExist, v);
	    	        break;
	        	case PositiveMatch:
	    	        GUITools.hideView(R.id.imgSubRatingExact, v);
	    	        GUITools.showView(R.id.imgSubRatingPositive, v);
	    	        GUITools.showView(R.id.imgSubRatingProbable, v);
	    	        GUITools.hideView(R.id.imgSubRatingSubsExist, v);
	        		break;
	        	case ProbableMatch:
	    	        GUITools.hideView(R.id.imgSubRatingExact, v);
	    	        GUITools.hideView(R.id.imgSubRatingPositive, v);
	    	        GUITools.showView(R.id.imgSubRatingProbable, v);
	    	        GUITools.hideView(R.id.imgSubRatingSubsExist, v);
	        		break;
	        	case NotMatched:
	    	        GUITools.hideView(R.id.imgSubRatingExact, v);
	    	        GUITools.hideView(R.id.imgSubRatingPositive, v);
	    	        GUITools.hideView(R.id.imgSubRatingProbable, v);
	    	        GUITools.hideView(R.id.imgSubRatingSubsExist, v);
	        		break;	        	
	        	}
	        }
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating subtitle list", e);
		}
			
        return v;
	}
}
	