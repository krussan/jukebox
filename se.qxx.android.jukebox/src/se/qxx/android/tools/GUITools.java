package se.qxx.android.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GUITools {

	public static void setTextOnTextview(int id, String text, View v) {
		TextView tv = (TextView)v.findViewById(id);
		if (tv != null)
			tv.setText(text);
	}
	
	public static void setImageOnImageView(int id, Bitmap bitmap, View v) {
		ImageView iv = (ImageView)v.findViewById(id);
		if (iv != null)
			iv.setImageBitmap(bitmap);
	}
	
	public static void setImageResourceOnImageView(int id, int resourceid, View v) {
		ImageView iv = (ImageView)v.findViewById(id);
		if (iv != null)
			iv.setImageResource(resourceid);
	}

	public static void setImageResourceOnImageButton(int id, int resourceid, View v) {
		ImageButton btn = (ImageButton)v.findViewById(id);
		if (btn != null)
			btn.setImageResource(resourceid);
	}
	
	public static void setImageOnImageView(int id, byte[] imageData, View v) {
		setImageOnImageView(id, getBitmapFromByteArray(imageData), v);
	}

	public static void setImageOnImageView(ImageView imageView, byte[] imageData) {
		imageView.setImageBitmap(getBitmapFromByteArray(imageData));
	}
	
	public static Bitmap getBitmapFromByteArray(byte[] imageData) {
    	return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
	}
	
	public static Bitmap scaleImage(int maxWidthOrHeight, Bitmap bitmap, Context context)
	{
	    // Get current dimensions AND the desired bounding box
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int bounding = dpToPx(maxWidthOrHeight, context);

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

	public static int dpToPx(int dp, Context c)
	{
	    float density = c.getApplicationContext().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	public static void flashBtn (int btnToFlashID, int replaceImageID, int timeout, View v){
		final ImageButton btnToFlash = (ImageButton)v.findViewById(btnToFlashID);
		if (btnToFlash != null) {
			btnToFlash.setBackgroundResource(replaceImageID);
		    Handler handler = new Handler(); 
		    handler.postDelayed(new Runnable() { 
		         public void run() { 
		        	 btnToFlash.setBackgroundResource(0);
		         } 
		    }, timeout);
		}
	} 
	
	public static void flashBtn (int btnToFlashID, int replaceImageID, View v){
		flashBtn(btnToFlashID, replaceImageID, 50, v);
	}	
	
	public static void flashBtnColor (int btnToFlashID, int color, int timeout, View v){
		final ImageButton btnToFlash = (ImageButton)v.findViewById(btnToFlashID);
		if (btnToFlash != null) {
			btnToFlash.setBackgroundColor(color);
		    Handler handler = new Handler(); 
		    handler.postDelayed(new Runnable() { 
		         public void run() { 
		        	 btnToFlash.setBackgroundColor(0);
		         } 
		    }, timeout);
		}
	}
	
	public static void flashBtnColor (int btnToFlashID, int color,View v){
		flashBtnColor(btnToFlashID, color, 50, v);
	}
	
	public static void flashBtnColor (int btnToFlashID, View v){
		flashBtnColor(btnToFlashID, Color.WHITE, 50, v);
	}
	
	public static void performHapticFeedback(int id, View v) {
		View view = v.findViewById(id);
		
		if (view!=null) {
			view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
		}
	}
	
	private static Vibrator vibr;
	public static void vibrate(long milliseconds, Context context) {
		if (vibr == null)
			vibr = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		
		vibr.vibrate(milliseconds);
	}
}

