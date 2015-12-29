package se.qxx.android.jukebox;

import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import android.view.View;

public class IncludeSubtitleRating extends JukeboxActivityBase {

	public static void initialize(Subtitle sub, View v) {
	    if (sub != null) {
	    	GUITools.setTextOnTextview(R.id.txtSubName, sub.getDescription(), v);
	    	GUITools.setTextOnTextview(R.id.lblSubLanguage, sub.getLanguage(), v);
	    	
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
}
