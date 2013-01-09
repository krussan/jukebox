package se.qxx.jukebox;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import se.qxx.jukebox.settings.Settings;

public class TcpListener implements Runnable {

	private boolean isRunning = false;
	public TcpListener() {
	}

	@Override
	public void run() {
		isRunning = true;
		
		try {

			Util.waitForSettings();
			
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
			Log.Error("Error while establishing client socket", Log.LogType.COMM, e);
			isRunning = false;
		}
	}
	
	public void stopListening() {
		this.isRunning = false;
	}

	
	
}
