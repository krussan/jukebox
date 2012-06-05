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
	
	private void sendCommand(String message, JukeboxRequestType type) {
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", message);

       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), type);
       	Thread t = new Thread(h);
       	t.start();				
	}
	
	public void onPlayClicked(View v) {
		sendCommand("Starting movie...", JukeboxRequestType.StartMovie);
	}

	public void onStopClicked(View v) {
		sendCommand("Stopping...", JukeboxRequestType.StopMovie);
	}
	
	public void onPauseClicked(View v) {
		sendCommand("Pausing...", JukeboxRequestType.PauseMovie);
	}
	
	public void onWakeupClicked(View v) {
		sendCommand("Waking up...", JukeboxRequestType.Wakeup);
	}
}
