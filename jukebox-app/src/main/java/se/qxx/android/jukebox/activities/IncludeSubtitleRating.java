package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class IncludeSubtitleRating {

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
				hideAll(v);
	    		break;	        	
	    	}
	    }
		
	}

	public static void hideAll(View v) {
		GUITools.hideView(R.id.imgSubRatingExact, v);
		GUITools.hideView(R.id.imgSubRatingPositive, v);
		GUITools.hideView(R.id.imgSubRatingProbable, v);
		GUITools.hideView(R.id.imgSubRatingSubsExist, v);
	}
}
