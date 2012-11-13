package se.qxx.android.jukebox;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.android.tools.SimpleGestureListener;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import se.qxx.android.tools.SimpleGestureFilter;

//public class MovieInfoActivity extends JukeboxActivityBase implements SimpleGestureListener {
//	SimpleGestureFilter detector;
//	
//	@Override
//	protected View getRootView() {
//		return this.findViewById(R.id.rootMovieItem);
//	}
//	
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//	    super.onCreate(savedInstanceState);
//	    setContentView(R.layout.movieitem);
//	    
//	    
//	    initializeView();
//	    
//	}
//	
//	private void initializeView() {
//	    Movie m = Model.get().getCurrentMovie();
//
//	    View rootView = this.getRootView();
//	    
//	    if (!m.getImage().isEmpty()) 
//	    	GUITools.setImageOnImageView(R.id.imageView1, m.getImage().toByteArray(), rootView);
//	    	    
//	    int duration = m.getDuration();
//	    int hours = duration / 60;
//	    int minutes = duration % 60;
//	    
//	    GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewStory, m.getStory(), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewGenre, String.format("Genre :: %s", StringUtils.join(m.getGenreList(), " / ")), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewDirector, String.format("Director :: %s", m.getDirector()), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewDuration, String.format("Duration :: %s h %s m", hours, minutes) , rootView);
//	    GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()) , rootView);
////	    GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()) , rootView);
//
//	    detector = new SimpleGestureFilter(this, this);
//	}
//		
//	public void onButtonClicked(View v) {
//		int id = v.getId();
//		GUITools.vibrate(28, this);
//		
//		switch (id) {
//			case R.id.btnPlay:
//				Intent iPlay = new Intent(this, NowPlayingActivity.class);
//				startActivity(iPlay);
//				break;	
//			case R.id.btnFullscreen:
//				sendCommand("Toggling fullscreen...", JukeboxRequestType.ToggleFullscreen);
//				break;
//			case R.id.btnPause:
//				sendCommand("Pausing...", JukeboxRequestType.PauseMovie);
//				break;
//			case R.id.btnStop:
//				sendCommand("Stopping...", JukeboxRequestType.StopMovie);
//				break;
//			case R.id.btnViewInfo:
//				String url = Model.get().getCurrentMovie().getImdbUrl();
//				if (url != null && url.length() > 0) {
//					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//					startActivity(browserIntent);
//				}
//				else {
//					Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
//				}
//				break;
//			case R.id.btnSubSelection:
//				Intent i = new Intent(this, SubSelectActivity.class);
//				startActivity(i);
//				break;
//			default:
//				break;
//		}
//	}
//
//    @Override 
//    public boolean dispatchTouchEvent(MotionEvent me){
//    	boolean b = super.dispatchTouchEvent(me);
//    	if (!b && this.detector != null)
//    		this.detector.onTouchEvent(me);
//    	
//    	return b;
//    }
//    
//	@Override
//	public void onSwipe(int direction) {
//		String str = "";
//		
//		switch (direction) {  
//		  case SimpleGestureFilter.SWIPE_RIGHT : 
//			  str = "Swipe Right";
//	  		  break;
//		  case SimpleGestureFilter.SWIPE_LEFT :  
//			  str = "Swipe Left";
//		      break;
//		  case SimpleGestureFilter.SWIPE_DOWN :  
//			  str = "Swipe Down";
//		      break;
//		  case SimpleGestureFilter.SWIPE_UP :    
//			  str = "Swipe Up";
//		      break;                                     
//		  } 
//		  Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
//	 }
//	
//	 @Override
//	 public void onDoubleTap() {
//	 }
//}
