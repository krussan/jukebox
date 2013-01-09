package se.qxx.android.jukebox;

import se.qxx.android.jukebox.adapters.MovieFragmentAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

public class FlipperActivity extends FragmentActivity {
 //SimpleGestureListener, AnimationListener {
//    private Animation animFlipInNext,animFlipOutNext, animFlipInPrevious, animFlipOutPrevious;
//	private ViewFlipper flipper;
	ViewPager pager;

//	SimpleGestureFilter detector;
	
	protected View getRootView() {
		return findViewById(R.id.rootViewPager);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemwrapper);
        pager = (ViewPager)this.getRootView();
        
        MovieFragmentAdapter mfa = new MovieFragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(mfa);
        
        pager.setCurrentItem(Model.get().getCurrentMovieIndex());
        
//        loadAnimations();
        
//	    initializeDetector();
	    
//        addViews();    
    }

//	protected void initializeDetector() {
//		detector = new SimpleGestureFilter(this, this);
//	    detector.setMode(SimpleGestureFilter.MODE_TRANSPARENT);
//	    DisplayMetrics metrics = GUITools.getDisplayMetrics(this);
//	    
//	    detector.setSwipeMaxDistance(Math.max(metrics.widthPixels, metrics.heightPixels));
//	    detector.setSwipeMinDistance(100);
//	}
//    
//    private void loadAnimations() {
//        animFlipInNext = AnimationUtils.loadAnimation(this, R.anim.flipinnext);
//        animFlipOutNext = AnimationUtils.loadAnimation(this, R.anim.flipoutnext);
//        animFlipInPrevious = AnimationUtils.loadAnimation(this, R.anim.flipinprevious);
//        animFlipOutPrevious = AnimationUtils.loadAnimation(this, R.anim.flipoutprevious);    	
//        
//		animFlipInNext.setAnimationListener(this);
//		animFlipInPrevious.setAnimationListener(this);        
//    }
    
//    private void addViews() {
//        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        
//        addView(Model.get().getPreviousMovie(), vi);
//        addView(Model.get().getCurrentMovie(), vi);
//        addView(Model.get().getNextMovie(), vi);
//
//        
//        flipper.setOutAnimation(null);
//        flipper.setInAnimation(null);
//        flipper.setDisplayedChild(1);
//    }
    
//    private void removeViews() {
//    	ViewGroup group = (ViewGroup)this.getRootView();
//    	group.removeAllViews();
//    }
//    
//    private void addView(Movie m, LayoutInflater vi) {
//        View v = vi.inflate(R.layout.movieitem, null);
//        initializeView(v, m);
//    	flipper.addView(v);
//    }
    

//    @Override 
//    public boolean dispatchTouchEvent(MotionEvent me){
//    	this.detector.onTouchEvent(me);
//    	return super.dispatchTouchEvent(me);
//    }
    
//	@Override
//	public void onSwipe(int direction) {
//		switch (direction) {  
//		  case SimpleGestureFilter.SWIPE_RIGHT : 
//			  moveNext();
//	  		  break;
//		  case SimpleGestureFilter.SWIPE_LEFT :  
//			  movePrevious();
//		      break;
//		  case SimpleGestureFilter.SWIPE_DOWN :  
//		      break;
//		  case SimpleGestureFilter.SWIPE_UP :    
//		      break;                                     
//		  } 
//	 }

//	private void moveNext() {
//		Model.get().currentMovieSetPrevious();		
//		flipper.setInAnimation(animFlipInNext);
//		flipper.setOutAnimation(animFlipOutNext);
//		flipper.showPrevious();
//	}
//	
//	private void movePrevious() {
//		Model.get().currentMovieSetNext();		
//		flipper.setInAnimation(animFlipInPrevious);
//		flipper.setOutAnimation(animFlipOutPrevious);
//		flipper.showNext();
//	}
	
//	 @Override
//	 public void onDoubleTap() {
//	 }
//
//	@Override
//	public void onAnimationEnd(Animation arg0) {
//		int index = flipper.indexOfChild(flipper.getCurrentView());
//
//		if (index != 1) {
//			removeViews();
//			addViews();		
//		}
//	}
//
//	@Override
//	public void onAnimationRepeat(Animation arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onAnimationStart(Animation arg0) {
//		// TODO Auto-generated method stub
//		
//	}	
	
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
//				startActivity(i);*
//				break;
			default:
				break;
		}
	}

//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		event.getX();
//		
//		
//
//        // Get the action that was done on this touch event
//        switch (event.getAction())
//        {
//        	case MotionEvent.ACTION_MOVE:
//
//        		
//            case MotionEvent.ACTION_DOWN:
//            {
//                // store the X value when the user's finger was pressed down
//                downXValue = event.getX();
//                break;
//            }
//
//            case MotionEvent.ACTION_UP:
//            {
//                // Get the X value when the user released his/her finger
//                float currentX = event.getX();            
//
//                // going backwards: pushing stuff to the right
//                if (downXValue < currentX)
//                {
//                    // Get a reference to the ViewFlipper
//                	// Set the animation
//                    vf.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
//                      // Flip!
//                      vf.showPrevious();
//                }
//
//                // going forwards: pushing stuff to the left
//                if (downXValue > currentX)
//                {
//                    // Get a reference to the ViewFlipper
//                    ViewFlipper vf = (ViewFlipper) findViewById(R.id.details);
//                     // Set the animation
//                     vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
//                      // Flip!
//                     vf.showNext();
//                }
//                break;
//            }
//        }
//
//        // if you return false, these actions will not be recorded
//        return true;	
//    }	
}
