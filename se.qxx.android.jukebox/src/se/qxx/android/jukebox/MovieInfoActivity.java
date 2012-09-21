package se.qxx.android.jukebox;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MovieInfoActivity extends JukeboxActivityBase {

	@Override
	protected View getRootView() {
		return this.findViewById(R.id.rootMovieItem);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.movieitem);
	    Movie m = Model.get().getCurrentMovie();

	    View rootView = this.getRootView();
	    
	    if (!m.getImage().isEmpty()) 
	    	GUITools.setImageOnImageView(R.id.imageView1, m.getImage().toByteArray(), rootView);
	    	    
	    int duration = m.getDuration();
	    int hours = duration / 60;
	    int minutes = duration % 60;
	    
	    GUITools.setTextOnTextview(R.id.textViewTitle, Html.fromHtml(m.getTitle()).toString(), rootView);
	    GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), rootView);
	    GUITools.setTextOnTextview(R.id.textViewStory, Html.fromHtml(m.getStory()).toString(), rootView);
	    GUITools.setTextOnTextview(R.id.textViewGenre, "Genre :: " + StringUtils.join(m.getGenreList(), " / "), rootView);
	    GUITools.setTextOnTextview(R.id.textViewDirector, "Director :: " + Html.fromHtml(m.getDirector()).toString(), rootView);
	    GUITools.setTextOnTextview(R.id.textViewDuration, String.format("Duration :: %s h %s m", hours, minutes) , rootView);
	    GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()) , rootView);
	   	    
	}
		
	public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.flashBtnColor(id, this.getRootView());
		GUITools.vibrate(28, this);
		
		switch (id) {
			case R.id.btnPlay:
				sendCommand("Starting movie...", JukeboxRequestType.StartMovie);
				break;	
			case R.id.btnFullscreen:
				sendCommand("Toggling fullscreen...", JukeboxRequestType.ToggleFullscreen);
				break;
			case R.id.btnPause:
				sendCommand("Pausing...", JukeboxRequestType.PauseMovie);
				break;
			case R.id.btnStop:
				sendCommand("Stopping...", JukeboxRequestType.StopMovie);
				break;
			case R.id.btnViewInfo:
				String url = Model.get().getCurrentMovie().getImdbUrl();
				if (url != null && url.length() > 0) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
				else {
					Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}
	
}
