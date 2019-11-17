package se.qxx.android.jukebox.widgets;

import java.util.Date;

import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

public class Seeker implements Runnable {

	private Thread internalThread;
	private boolean isRunning = false;
	private SeekerListener listener;
    private JukeboxSettings settings;

    public Seeker(SeekerListener listener, JukeboxSettings settings) {
		this.listener = listener;
        this.settings = settings;
    }
	
	public void stop() {
		this.isRunning = false;
	}
		
	private void getTime() {
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(
       			settings.getServerIpAddress(),
                settings.getServerPort());
       	
       	h.getTime(settings.getCurrentMediaPlayer(), response -> {
            // The time command also returns the name of the currently playing file.
            // If it differs from the model then set the current media
			if (response != null && listener != null)
                   listener.updateSeeker(((JukeboxDomain.JukeboxResponseTime)response).getSeconds(), 0);
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

	private void startNewThread() {
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
