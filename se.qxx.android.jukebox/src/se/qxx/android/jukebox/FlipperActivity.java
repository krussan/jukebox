package se.qxx.android.jukebox;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.SimpleGestureFilter;
import se.qxx.android.tools.SimpleGestureListener;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class FlipperActivity extends JukeboxActivityBase implements SimpleGestureListener, AnimationListener {
    private Animation animFlipInNext,animFlipOutNext, animFlipInPrevious, animFlipOutPrevious;
	private ViewFlipper flipper;

	SimpleGestureFilter detector;
	
	@Override
	protected View getRootView() {
		return findViewById(R.id.rootFlipper);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemwrapper);
        flipper = (ViewFlipper)this.getRootView();
        
        loadAnimations();
        addViews();
        
	    initializeDetector();
    }

	protected void initializeDetector() {
		detector = new SimpleGestureFilter(this, this);
	    detector.setMode(SimpleGestureFilter.MODE_TRANSPARENT);
	    DisplayMetrics metrics = GUITools.getDisplayMetrics(this);
	    
	    detector.setSwipeMaxDistance(Math.max(metrics.widthPixels, metrics.heightPixels));
	    detector.setSwipeMinDistance(100);
	}
    
    private void loadAnimations() {
        animFlipInNext = AnimationUtils.loadAnimation(this, R.anim.flipinnext);
        animFlipOutNext = AnimationUtils.loadAnimation(this, R.anim.flipoutnext);
        animFlipInPrevious = AnimationUtils.loadAnimation(this, R.anim.flipinprevious);
        animFlipOutPrevious = AnimationUtils.loadAnimation(this, R.anim.flipoutprevious);    	
        
		animFlipInNext.setAnimationListener(this);
		animFlipInPrevious.setAnimationListener(this);        
    }
    
    private void addViews() {
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        addView(Model.get().getPreviousMovie(), vi);
        addView(Model.get().getCurrentMovie(), vi);
        addView(Model.get().getNextMovie(), vi);

        flipper.setOutAnimation(null);
        flipper.setInAnimation(null);
        flipper.setDisplayedChild(1);
    }
    
    private void removeViews() {
    	ViewGroup group = (ViewGroup)this.getRootView();
    	group.removeAllViews();
    }
    
    private void addView(Movie m, LayoutInflater vi) {
        View v = vi.inflate(R.layout.movieitem, null);
        initializeView(v, m);
    	flipper.addView(v);
    }
    
	private void initializeView(View v, Movie m) {
	    if (!m.getImage().isEmpty()) 
	    	GUITools.setImageOnImageView(R.id.imageView1, m.getImage().toByteArray(), v);
	    	    
	    int duration = m.getDuration();
	    int hours = duration / 60;
	    int minutes = duration % 60;
	    
	    GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), v);
	    GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), v);
	    GUITools.setTextOnTextview(R.id.textViewStory, m.getStory(), v);
	    GUITools.setTextOnTextview(R.id.textViewGenre, String.format("Genre :: %s", StringUtils.join(m.getGenreList(), " / ")), v);
	    GUITools.setTextOnTextview(R.id.textViewDirector, String.format("Director :: %s", m.getDirector()), v);
	    GUITools.setTextOnTextview(R.id.textViewDuration, String.format("Duration :: %s h %s m", hours, minutes) , v);
	    GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()), v);
	    //GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()), v);

		MovieMediaLayoutAdapter adapter = new MovieMediaLayoutAdapter(this, m); 
		ListView listView = (ListView)v.findViewById(R.id.listViewFilename);
		listView.setAdapter(adapter);
	    
	    //detector = new SimpleGestureFilter(this, this);
	}    

    @Override 
    public boolean dispatchTouchEvent(MotionEvent me){
    	this.detector.onTouchEvent(me);
    	return super.dispatchTouchEvent(me);
    }
    
	@Override
	public void onSwipe(int direction) {
		switch (direction) {  
		  case SimpleGestureFilter.SWIPE_RIGHT : 
			  moveNext();
	  		  break;
		  case SimpleGestureFilter.SWIPE_LEFT :  
			  movePrevious();
		      break;
		  case SimpleGestureFilter.SWIPE_DOWN :  
		      break;
		  case SimpleGestureFilter.SWIPE_UP :    
		      break;                                     
		  } 
	 }

	private void moveNext() {
		Model.get().currentMovieSetPrevious();		
		flipper.setInAnimation(animFlipInNext);
		flipper.setOutAnimation(animFlipOutNext);
		flipper.showPrevious();
	}
	
	private void movePrevious() {
		Model.get().currentMovieSetNext();		
		flipper.setInAnimation(animFlipInPrevious);
		flipper.setOutAnimation(animFlipOutPrevious);
		flipper.showNext();
	}
	
	 @Override
	 public void onDoubleTap() {
	 }

	@Override
	public void onAnimationEnd(Animation arg0) {
		int index = flipper.indexOfChild(flipper.getCurrentView());

		if (index != 1) {
			removeViews();
			addViews();		
		}
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}	
	
	public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, this);
		
		switch (id) {
			case R.id.btnPlay:
				Intent iPlay = new Intent(this, NowPlayingActivity.class);
				startActivity(iPlay);
				break;	
//			case R.id.btnFullscreen:
//				sendCommand("Toggling fullscreen...", JukeboxRequestType.ToggleFullscreen);
//				break;
//			case R.id.btnPause:
//				sendCommand("Pausing...", JukeboxRequestType.PauseMovie);
//				break;
//			case R.id.btnStop:
//				sendCommand("Stopping...", JukeboxRequestType.StopMovie);
//				break;
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
//			case R.id.btnSubSelection:
//				Intent i = new Intent(this, SubSelectActivity.class);
//				startActivity(i);
//				break;
			default:
				break;
		}
	}	
}
