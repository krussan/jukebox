package se.qxx.android.jukebox;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NowPlayingActivity extends JukeboxActivityBase 
	implements OnSeekBarChangeListener, SeekerListener, JukeboxResponseListener, ModelUpdatedEventListener {

	private Seeker seeker;
	private boolean isManualSeeking = false;
	
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
	    
	    sendCommand(this, "Checking status", JukeboxRequestType.IsPlaying);
	    
	    Model.get().addEventListener(this);
	}
	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {		
		final TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);
		
		if (this.isManualSeeking)
			runOnUiThread(new UpdateSeekIndicator(progress, tv));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		this.isManualSeeking = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		final TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);

		int seconds = seekBar.getProgress();
		sendCommand(this, "Seeking...", JukeboxRequestType.Seek, seconds);
		runOnUiThread(new UpdateSeekIndicator(seconds, tv, seekBar));		
		
		this.isManualSeeking = false;
	}
	
    public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, this);
		
		switch (id) {
			case R.id.btnPlay:
				sendCommand(this, "Starting movie...", JukeboxRequestType.StartMovie);
				break;	
			case R.id.btnFullscreen:
				sendCommand("Toggling fullscreen...", JukeboxRequestType.ToggleFullscreen);
				break;
			case R.id.btnPause:
				seeker.toggle();
				sendCommand("Pausing...", JukeboxRequestType.PauseMovie);
				break;
			case R.id.btnStop:
				seeker.stop();
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
				pickSubtitle();
//				Intent i = new Intent(this, SubSelectActivity.class);
//				startActivity(i);
				break;
			default:
				break;
		}
    }	

	@Override
	public void updateSeeker(final int seconds) {
		final TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);
		final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBarDuration);
		
		if (!this.isManualSeeking)
			runOnUiThread(new UpdateSeekIndicator(seconds, tv, seekBar));
	}
	
	@Override
	public void increaseSeeker(int advanceSeconds) {
		final TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);		
		final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBarDuration);
		int seconds = seekBar.getProgress();

		if (!this.isManualSeeking)
			runOnUiThread(new UpdateSeekIndicator(seconds + advanceSeconds, tv, seekBar));
	}

	
	@Override
	public void onResponseReceived(JukeboxResponse resp) {
		if (resp.getType() == JukeboxRequestType.IsPlaying) {								
			try {
				boolean isPlaying = JukeboxResponseIsPlaying.parseFrom(resp.getArguments()).getIsPlaying();
				if (isPlaying) {
					sendCommand(this, "Checking current playing movie...", JukeboxRequestType.GetTitle);
				}
				else {
				    sendCommand(this, "Starting movie...", JukeboxRequestType.StartMovie);						
				}
			} catch (InvalidProtocolBufferException e) {
				Log.e(getLocalClassName(), "Error while parsing response from IsPlaying", e);
			}
		}		
		
		if (resp.getType() == JukeboxRequestType.GetTitle) {
			try {
				String playerFilename = JukeboxResponseGetTitle.parseFrom(resp.getArguments()).getTitle();
				String currentFilename = Model.get().getCurrentMovie().getFilename();
				
				if (StringUtils.equalsIgnoreCase(playerFilename, currentFilename)) {
					//initialize seeker and get subtitles if app has been reinitialized
					sendCommand(this, "Getting subtitles", JukeboxRequestType.ListSubtitles);			
					seeker.start();
				}
				else {
					// stop movie and start new
					sendCommand(this, "Stopping movie", JukeboxRequestType.StopMovie);
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

		if (resp.getType() == JukeboxRequestType.StopMovie) {
			sendCommand(this, "Starting movie", JukeboxRequestType.StartMovie);
		}
	}

	private void pickSubtitle() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Pick subtitle");
		b.setItems(Model.get().getSubtitleDescriptions(), new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Model.get().setCurrentSubtitle(arg1);
			}
			
		});
		
		b.show();
	}

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent)e;
		
		if (ev.getType() == ModelUpdatedType.CurrentSub) {
			this.sendCommand("Switching subtitle", JukeboxRequestType.SetSubtitle, Model.get().getCurrentSubtitleID());
		}
	}

}
