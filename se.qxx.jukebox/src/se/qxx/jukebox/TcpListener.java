package se.qxx.jukebox;

import java.io.IOException;
import java.net.*;

import se.qxx.jukebox.settings.Settings;

public class TcpListener implements Runnable {

	private boolean isRunning = false;
	public TcpListener() {
	}

	@Override
	public void run() {
		isRunning = true;
		
		try {

			while (Settings.get() == null) {
				Log.Info("Settings has not been initialized. Sleeping for 10 seconds");
				Thread.sleep(500);
			}
			
			int port = Settings.get().getTcpListener().getPort().getValue();
			ServerSocket socket;

			socket = new ServerSocket(port);
			socket.setSoTimeout(500);
			
			while (isRunning) {
				try {
					Socket client = socket.accept();
					
					Thread t = new Thread(new TcpConnection(client));
					t.start();
				}
				catch (SocketTimeoutException ex) {
					// Loop just to be able to shutdown
				}				
			}			
		} 
		catch (IOException e) {
			Log.Error("Error while establishing client socket", e);
			isRunning = false;
		}
		catch (InterruptedException e) {
			Log.Error("Settings has not been initialized", e);
		}		
	}
	
	public void stopListening() {
		this.isRunning = false;
	}

	
	
}
