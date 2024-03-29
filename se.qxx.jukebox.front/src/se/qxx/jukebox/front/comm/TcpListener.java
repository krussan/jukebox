package se.qxx.jukebox.front.comm;

import se.qxx.jukebox.comm.JukeboxFrontServer;
import se.qxx.jukebox.front.FrontSettings;

public class TcpListener implements Runnable {

	private boolean isRunning = false;
	public TcpListener() {
	}

	@Override
	public void run() {
		isRunning = true;
		  				
		int port = FrontSettings.get().getPort();
		JukeboxFrontServer server = new JukeboxFrontServer(port);
  
		server.runServer(JukeboxFrontServerConnection.class);

		while (isRunning) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				this.isRunning = false;
			}				
		}
		
		server.stopServer();
	}
	
	public void stopListening() {
		this.isRunning = false;
	}

}
