package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public class IncludeSubtitleRating {

	public static void initialize(Subtitle sub, View v) {
	    if (sub != null) {
	    	GUITools.setTextOnTextview(R.id.txtSubName, sub.getDescription(), v);
	    	GUITools.setTextOnTextview(R.id.lblSubLanguage, sub.getLanguage(), v);

			setSubtitleRating(sub, v);
	    }
	}

	public static void setSubtitleRating(Subtitle sub, View v) {
		if (sub != null) {
			Rating rating = sub.getRating();

			switch (rating) {
				case SubsExist:
					GUITools.setImageResourceOnImageView(R.id.imgSub, R.drawable.ic_star_subsexist, v);
					break;
				case ExactMatch:
					GUITools.setImageResourceOnImageView(R.id.imgSub, R.drawable.ic_star_exact, v);
					break;
				case PositiveMatch:
					GUITools.setImageResourceOnImageView(R.id.imgSub, R.drawable.ic_star_positive, v);
					break;
				case ProbableMatch:
					GUITools.setImageResourceOnImageView(R.id.imgSub, R.drawable.ic_star_probable, v);
					break;
				case NotMatched:
					GUITools.hideView(R.id.imgSub, v);
					break;
			}
		}
		else {
			GUITools.hideView(R.id.imgSub, v);
		}
	}

}
