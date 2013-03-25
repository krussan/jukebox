package se.qxx.android.jukebox;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.RpcCallback;

public class NowPlayingActivity extends JukeboxActivityBase 
	implements OnSeekBarChangeListener, SeekerListener {

	private Seeker seeker;
	private boolean isManualSeeking = false;
	private JukeboxConnectionHandler comm = new JukeboxConnectionHandler();

	private class OnStatusComplete implements RpcCallback<JukeboxResponseIsPlaying> {
		@Override
		public void run(JukeboxResponseIsPlaying response) {
			Logger.Log().d("Response --- IsPlaying");
			if (response.getIsPlaying()) {
				Logger.Log().d("Request --- GetTitle");
				comm.getTitle(JukeboxSettings.get().getCurrentMediaPlayer(), new OnGetTitleComplete());
			}
			else {
				Logger.Log().d("Request --- StartMovie");
				comm.startMovie(
						JukeboxSettings.get().getCurrentMediaPlayer(),
						Model.get().getCurrentMovie(), new OnStartMovieComplete());
			}
		}
	}
	
	private class OnGetTitleComplete implements RpcCallback<JukeboxResponseGetTitle> {
		@Override
		public void run(JukeboxResponseGetTitle response) {
			Logger.Log().d("Response --- GetTitle");
			String playerFilename = response.getTitle();
			final Media md = matchCurrentFilenameAgainstMedia(playerFilename);
			if (md != null) {
				//initialize seeker and get subtitles if app has been reinitialized
				Model.get().setCurrentMedia(md);
				initializeSeeker();
				
				Thread t1 = new Thread(new Runnable(){
					@Override
					public void run() {
						Logger.Log().d("Request --- ListSubtitles");
						comm.listSubtitles(md);						
					}
				});
				t1.start();
				
				//Start seeker and get time asap as the movie is playing
				seeker.start(true);
			}
			else {
				Thread t2 = new Thread(new Runnable(){
					@Override
					public void run() {
						Logger.Log().d("Request --- StopMovie");
						comm.stopMovie(JukeboxSettings.get().getCurrentMediaPlayer(), new OnStopMovieComplete());
					}
				});
				t2.start();
			}
		}
	}

	private class OnStartMovieComplete implements RpcCallback<JukeboxResponseStartMovie> {
		@Override
		public void run(JukeboxResponseStartMovie response) {
			Logger.Log().d("Response --- StartMovie");
			Model.get().setCurrentMedia(0);			
			initializeSeeker();			
			seeker.start();		
			
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					Logger.Log().d("Request --- ListSubtitles");
					comm.listSubtitles(Model.get().getCurrentMedia());
				}
			});
			t.start();
		}
	}

	private class OnStopMovieComplete implements RpcCallback<Empty> {
		@Override
		public void run(Empty arg0) {
			Logger.Log().d("Response --- StopMovie");

			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					Logger.Log().d("Request --- StartMovie");				
					comm.startMovie(JukeboxSettings.get().getCurrentMediaPlayer(), Model.get().getCurrentMovie(), new OnStartMovieComplete());				}
			});
			t.start();			
		}		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.nowplaying);
        
        initializeView();
    }
	
	@Override 
	protected void onPause() {
		super.onPause();
		if (seeker != null)
			seeker.stop();
	};
	
	@Override
	protected void onStop() {
		super.onStop();
		if (seeker != null)
			seeker.stop();
	}
	
	@Override protected void onResume() {
		super.onResume();
		
		if (seeker != null)
			seeker.start();
	};
		
	private void initializeView() {
		try {
		    Movie m = Model.get().getCurrentMovie();
		    seeker = new Seeker(this);
		    View rootView = this.getRootView();
		    
		    if (!m.getImage().isEmpty()) {
		    	Bitmap bm = GUITools.getBitmapFromByteArray(m.getImage().toByteArray());
	//	    	DisplayMetrics metrics = GUITools.getDisplayMetrics(this);
		    	Bitmap scaledImage = GUITools.scaleImage(300, bm, rootView.getContext());
		    	GUITools.setImageOnImageView(R.id.imgNowPlaying, scaledImage, rootView);	
		    }
	
		    GUITools.setTextOnTextview(R.id.lblNowPlayingTitle, m.getTitle(), rootView);
		    
	//		MovieMediaLayoutAdapter adapter = new MovieMediaLayoutAdapter(this, m); 
	//		ListView v = (ListView)findViewById(R.id.listViewFilename);
	//		v.setAdapter(adapter);
	
		    SeekBar sb = (SeekBar)findViewById(R.id.seekBarDuration);
			sb.setOnSeekBarChangeListener(this);
	
			Logger.Log().d("Request -- IsPlaying");
			comm.isPlaying(JukeboxSettings.get().getCurrentMediaPlayer(), new OnStatusComplete());
				    
	//	    Model.get().addEventListener(this);
		}
		catch (Exception e) {
			Logger.Log().e("Unable to initialize NowPlayingActivity", e);
		}
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
		
		Logger.Log().d("Request --- Seek");
		comm.seek(JukeboxSettings.get().getCurrentMediaPlayer(), seconds);
		
		this.isManualSeeking = false;
	}
	
    public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, this);
		String player = JukeboxSettings.get().getCurrentMediaPlayer();
		
		switch (id) {
			case R.id.btnPlay:
				Logger.Log().d("Request --- StartMovie");
				comm.startMovie(player, Model.get().getCurrentMovie(), new OnStartMovieComplete());
				break;	
			case R.id.btnFullscreen:
				Logger.Log().d("Request --- ToggleFullScreen");
				comm.toggleFullscreen(player);
				break;
			case R.id.btnPause:
				Logger.Log().d("Request --- Pause");
				seeker.toggle();
				comm.pauseMovie(player);
				break;
			case R.id.btnStop:
				Logger.Log().d("Request --- StopMove");
				seeker.stop();
				comm.stopMovie(player, null);
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


	private void initializeSeeker() {
	    SeekBar sb = (SeekBar)findViewById(R.id.seekBarDuration);
	    if (sb != null) 
	    	sb.setMax(Model.get().getCurrentMedia().getMetaDuration());
	}
		
	protected Media matchCurrentFilenameAgainstMedia(String playerFilename) {
		for (Media md : Model.get().getCurrentMovie().getMediaList()) {
			if (StringUtils.equalsIgnoreCase(playerFilename, md.getFilename())) {
				return md;
			}
		}
		
		return null;
	}

}
