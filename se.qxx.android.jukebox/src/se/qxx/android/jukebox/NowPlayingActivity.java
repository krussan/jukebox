package se.qxx.android.jukebox;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.DialogInterface.OnDismissListener;

public class NowPlayingActivity extends JukeboxActivityBase implements OnSeekBarChangeListener, SeekerListener, JukeboxResponseListener {

	private Seeker seeker;
	
	@Override
	protected View getRootView() {
		// TODO Auto-generated method stub
		return findViewById(R.id.rootNowPlaying);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.nowplaying);
        
        initializeView();
    }
	
	@Override
	protected void onStop() {
		super.onStop();
		if (seeker != null)
			seeker.stop();
	}
		
	private void initializeView() {
	    Movie m = Model.get().getCurrentMovie();
	    seeker = new Seeker(this);
	    View rootView = this.getRootView();
	    
	    if (!m.getImage().isEmpty()) 
	    	GUITools.setImageOnImageView(R.id.imgSubPoster, m.getImage().toByteArray(), rootView);
	    
	    GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), rootView);
	    GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), rootView);
	    GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()) , rootView);
	    
	    SeekBar sb = (SeekBar)findViewById(R.id.seekBarDuration);
	    if (sb != null) {
	    	sb.setMax(m.getMetaDuration());
	    	sb.setOnSeekBarChangeListener(this);
	    }
	    
	    //TODO: setProgress if movie is actually playing
	    sendCommand(this, "Checking status", JukeboxRequestType.IsPlaying);
	}
	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);
		runOnUiThread(new UpdateSeekIndicator(progress, tv));		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//TODO: send seek command to media player
		int seconds = seekBar.getProgress();
		sendCommand("Seeking...", JukeboxRequestType.Seek, seconds);
	}
	
    public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, this);
		
		switch (id) {
			case R.id.btnPlay:
				sendCommand("Starting movie...", JukeboxRequestType.StartMovie);
				sendCommand("Getting subtitles...", JukeboxRequestType.ListSubtitles);
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
			case R.id.btnSubSelection:
				Intent i = new Intent(this, SubSelectActivity.class);
				startActivity(i);
				break;
			default:
				break;
		}
    }	

	@Override
	public void updateSeeker(final int seconds) {
		final TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);
		
		runOnUiThread(new UpdateSeekIndicator(seconds, tv));
	}

	
	@Override
	public void onResponseReceived(JukeboxResponse resp) {
		if (resp.getType() == JukeboxRequestType.IsPlaying) {								
			try {
				boolean isPlaying = JukeboxResponseIsPlaying.parseFrom(resp.getArguments()).getIsPlaying();
				if (isPlaying) {
					sendCommand(this, "Checking current playing movie...", JukeboxRequestType.GetTitle);
				}
				else
				    sendCommand(this, "Starting movie...", JukeboxRequestType.StartMovie);						
			} catch (InvalidProtocolBufferException e) {
				Log.e(getLocalClassName(), "Error while parsing response from IsPlaying", e);
			}
		}		
		
		if (resp.getType() == JukeboxRequestType.GetTitle) {
			try {
				String title = JukeboxResponseGetTitle.parseFrom(resp.getArguments()).getTitle();
				String currentTitle = Model.get().getCurrentMovie().getFilename();
				
				if (StringUtils.equalsIgnoreCase(title, currentTitle)) {
					//initialize seeker
					seeker.start();
				}
				else {
					// stop movie and start new
					sendCommand(this, "Stopping movie", JukeboxRequestType.StopMovie);
					sendCommand(this, "Starting movie", JukeboxRequestType.StartMovie);
				}
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				Log.e(getLocalClassName(), "Error while parsing response from GetTitle", e);
			}
		}
		
		if (resp.getType() == JukeboxRequestType.StartMovie) {
			sendCommand(this, "Getting subtitles", JukeboxRequestType.ListSubtitles);			
			seeker.start();
		}
		
	}


}
