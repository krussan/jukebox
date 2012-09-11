package se.qxx.android.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GUITools {

	public static void setTextOnTextview(int id, String text, Activity activity) {
		TextView tv = (TextView)activity.findViewById(id);
		tv.setText(text);
	}
	
	public static void setImageOnImageView(int id, Bitmap bitmap, Activity activity) {
		ImageView iv = (ImageView)activity.findViewById(id);
		iv.setImageBitmap(bitmap);
	}
	
	public static void setImageOnImageView(int id, byte[] imageData, Activity activity) {
		setImageOnImageView(id, getBitmapFromByteArray(imageData), activity);
	}
	
	public static Bitmap getBitmapFromByteArray(byte[] imageData) {
    	return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
	}
	
	public static Bitmap scaleImage(int maxWidthOrHeight, Bitmap bitmap, Activity activity)
	{
	    // Get current dimensions AND the desired bounding box
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int bounding = dpToPx(maxWidthOrHeight, activity);

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.  
	    float xScale = ((float) bounding) / width;
	    float yScale = ((float) bounding) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView 
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    width = scaledBitmap.getWidth(); // re-use
	    height = scaledBitmap.getHeight(); // re-use

	    return scaledBitmap;
	}

	public static int dpToPx(int dp, Activity activity)
	{
	    float density = activity.getApplicationContext().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	public static void flashBtn (int btnToFlashID, int replaceImageID, int timeout, Activity activity){
		final ImageButton btnToFlash = (ImageButton)activity.findViewById(btnToFlashID);
		btnToFlash.setBackgroundResource(replaceImageID);
	    Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 btnToFlash.setBackgroundResource(0);
	         } 
	    }, timeout);
	}
	
	public static void flashBtn (int btnToFlashID, int replaceImageID, Activity activity){
		flashBtn(btnToFlashID, replaceImageID, 50, activity);
	}	
	
	public static void flashBtnColor (int btnToFlashID, int color, int timeout, Activity activity){
		final ImageButton btnToFlash = (ImageButton)activity.findViewById(btnToFlashID);
		btnToFlash.setBackgroundColor(color);
	    Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 btnToFlash.setBackgroundColor(0);
	         } 
	    }, timeout);
	}
	
	public static void flashBtnColor (int btnToFlashID, int color,Activity activity){
		flashBtnColor(btnToFlashID, color, 50, activity);
	}
	
	public static void flashBtnColor (int btnToFlashID, Activity activity){
		flashBtnColor(btnToFlashID, Color.WHITE, 50, activity);
	}
	
	public static void performHapticFeedback(int id, Activity activity) {
		View v = activity.findViewById(id);
		v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
	}
}

