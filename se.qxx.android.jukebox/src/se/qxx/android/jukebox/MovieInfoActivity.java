package se.qxx.android.jukebox;

import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MovieInfoActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.movieitem);
	    Movie m = Model.get().getCurrentMovie();
	    		
	    TextView tv = (TextView)findViewById(R.id.textView1);
	    tv.setText(m.getTitle());
	}
	
	public void onPlayClicked(View v) {
    	Logger.Log().i("onPlayClicked");
    	
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", "Starting movie...");

       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), JukeboxRequestType.StartMovie);
       	Thread t = new Thread(h);
       	t.start();		
	}

	public void onStopClicked(View v) {
		
	}
}
