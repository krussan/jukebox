package se.qxx.android.jukebox;

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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MovieInfoActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.movieitem);
	    Movie m = Model.get().getCurrentMovie();
	    		
	    TextView tv = (TextView)findViewById(R.id.textView1);
	    tv.setText(m.getTitle());
	    
	    if (!m.getImage().isEmpty()) {
	    	ImageView iv = (ImageView)findViewById(R.id.imageView1);
	    	byte[] imageData = m.getImage().toByteArray();
	    	
	    	Bitmap bm = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
	    	iv.setImageBitmap(bm);
	    }
	}
	
	private void sendCommand(String message, JukeboxRequestType type) {
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", message);

       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), type);
       	Thread t = new Thread(h);
       	t.start();				
	}
		
	public void onButtonClicked(View v) {
		switch (v.getId()) {
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
			case R.id.btnWakeup:
				sendCommand("Waking up...", JukeboxRequestType.Wakeup);
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
	
	/*
	private void scaleImage()
	{
	    // Get the ImageView and its bitmap
	    ImageView view = (ImageView) findViewById(R.id.image_box);
	    Drawable drawing = view.getDrawable();
	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    // Get current dimensions AND the desired bounding box
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int bounding = dpToPx(250);
	    Log.i("Test", "original width = " + Integer.toString(width));
	    Log.i("Test", "original height = " + Integer.toString(height));
	    Log.i("Test", "bounding = " + Integer.toString(bounding));

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.  
	    float xScale = ((float) bounding) / width;
	    float yScale = ((float) bounding) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;
	    Log.i("Test", "xScale = " + Float.toString(xScale));
	    Log.i("Test", "yScale = " + Float.toString(yScale));
	    Log.i("Test", "scale = " + Float.toString(scale));

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView 
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    width = scaledBitmap.getWidth(); // re-use
	    height = scaledBitmap.getHeight(); // re-use
	    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
	    Log.i("Test", "scaled width = " + Integer.toString(width));
	    Log.i("Test", "scaled height = " + Integer.toString(height));

	    // Apply the scaled bitmap
	    view.setImageDrawable(result);

	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams(); 
	    params.width = width;
	    params.height = height;
	    view.setLayoutParams(params);

	    Log.i("Test", "done");
	    
	}

*/
	
	private int dpToPx(int dp)
	{
	    float density = getApplicationContext().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
}
