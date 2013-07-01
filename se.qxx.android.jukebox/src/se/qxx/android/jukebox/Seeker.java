package se.qxx.android.jukebox;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;

import com.google.protobuf.RpcCallback;

public class Seeker implements Runnable {

	private Thread internalThread;
	private boolean isRunning = false;
	private SeekerListener listener;
	
	public Seeker(SeekerListener listener) {
		this.listener = listener;
	}
	
	public void stop() {
		this.isRunning = false;
	}
		
	private void getTime() {
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(
       			JukeboxSettings.get().getServerIpAddress()
       			, JukeboxSettings.get().getServerPort());
       	
       	h.getTime(JukeboxSettings.get().getCurrentMediaPlayer(), new RpcCallback<JukeboxResponseTime>() {
			@Override
			public void run(JukeboxResponseTime response) {				
				// The time command also returns the name of the currently playing file.
				// If it differs from the model then set the current media
				if (!StringUtils.equalsIgnoreCase(response.getFilename(), Model.get().getCurrentMedia().getFilename()))
					Model.get().setCurrentMedia(response.getFilename());
				
				if (listener != null)
					listener.updateSeeker(response.getSeconds());				
			}
		});
	}

	@Override
	public void run() {
		this.isRunning = true;
		while (this.isRunning) {
			try {
				long millis = (new Date()).getTime();
				
				while ((new Date()).getTime() - millis < 15000 && this.isRunning) {
					Thread.sleep(1000);
					if (this.listener != null)
						this.listener.increaseSeeker(1);
				}
				
				getTime(); 				
			} catch (InterruptedException e) {
			}
		}
	}

	public void start(boolean immediate) {
		if (internalThread != null) {
			if (internalThread.isAlive()) 
				this.isRunning = true;
			else {
				startNewThread();
			}
		}
		else {
			startNewThread();			
		}	
		
		if (immediate)
			getTime();
	}
	
	public void start() {
		this.start(false);
	}

	protected void startNewThread() {
		internalThread = new Thread(this);
		internalThread.start();
	}
	
	public void toggle() {
		if (this.isRunning)
			this.stop();
		else
			this.start();
	}
}
