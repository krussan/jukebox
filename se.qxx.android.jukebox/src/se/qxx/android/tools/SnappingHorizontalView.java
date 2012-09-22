package se.qxx.android.tools;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class SnappingHorizontalView<T> extends HorizontalScrollView {
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
 
    private ArrayList<T> items = null;
    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;
 
    public SnappingHorizontalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setup();
    }
 
    public SnappingHorizontalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }
 
    public SnappingHorizontalView(Context context) {
        super(context);
        setup();
    }

    private void setup() {
    	mGestureDetector = new GestureDetector(new MyGestureDetector());
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //If the user swipes
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
            int scrollX = getScrollX();
            int featureWidth = getMeasuredWidth();
            mActiveFeature = ((scrollX + (featureWidth / 2)) / featureWidth);
            int scrollTo = mActiveFeature * featureWidth;
            smoothScrollTo(scrollTo, 0);
            return true;
        }
        else{
            return false;
        }    	
    }
    
    private class MyGestureDetector extends SimpleOnGestureListener {
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        try {
	            //right to left
	            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                int featureWidth = getMeasuredWidth();
	                mActiveFeature = (mActiveFeature < (items.size() - 1))? mActiveFeature + 1 : items.size() -1;
	                smoothScrollTo(mActiveFeature*featureWidth, 0);
	                return true;
	            }
	            //left to right
	            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                int featureWidth = getMeasuredWidth();
	                mActiveFeature = (mActiveFeature > 0)? mActiveFeature - 1:0;
	                smoothScrollTo(mActiveFeature*featureWidth, 0);
	                return true;
	            }
	        } catch (Exception e) {
	        	Log.e("Fling", "There was an error processing the Fling event:" + e.getMessage());
	        }
	        return false;
	    }
    }
}	