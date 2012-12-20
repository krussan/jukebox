package se.qxx.android.jukebox;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.comm.JukeboxResponseListener;
import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;

public class Seeker implements JukeboxResponseListener, Runnable {

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
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(this, JukeboxRequestType.Time);
     	Thread t = new Thread(h);
       	t.start();
	}

	@Override
	public void onResponseReceived(JukeboxResponse resp) {
		JukeboxRequestType type = resp.getType();
		if (type == JukeboxRequestType.Time) {
			try {
				JukeboxResponseTime respTime = JukeboxResponseTime.parseFrom(resp.getArguments());
				
				// The time command also returns the name of the currently playing file.
				// If it differs from the model then set the current media
				if (!StringUtils.equalsIgnoreCase(respTime.getFilename(), Model.get().getCurrentMedia().getFilename()))
					Model.get().setCurrentMedia(respTime.getFilename());
				
				if (this.listener != null)
					this.listener.updateSeeker(respTime.getSeconds());
				
			} catch (InvalidProtocolBufferException e) {
			}
			
		}
	}

	@Override
	public void run() {
		this.isRunning = true;
		while (this.isRunning) {
			try {
				getTime();

				long millis = (new Date()).getTime();
				
				while ((new Date()).getTime() - millis < 15000 && this.isRunning) {
					Thread.sleep(1000);
					if (this.listener != null)
						this.listener.increaseSeeker(1);
				}
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void start() {
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
