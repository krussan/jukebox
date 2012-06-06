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
			default:
				break;
		}
	}
}
