package se.qxx.jukebox;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import se.qxx.jukebox.comm.JukeboxRpcServer;
import se.qxx.jukebox.settings.Settings;

public class TcpListener implements Runnable {

	private boolean isRunning = false;
	public TcpListener() {
	}

	@Override
	public void run() {
		isRunning = true;
		  
		Util.waitForSettings();
					
		int port = Settings.get().getTcpListener().getPort().getValue();
		JukeboxRpcServer server = new JukeboxRpcServer(port);
  
//			ServerSocket socket;
//
//			socket = new ServerSocket(port);
//			socket.setSoTimeout(500);
		
		while (isRunning) {
			try {
				Thread.sleep(1000);
//					Socket client = socket.accept();
//					
//					Thread t = new Thread(new TcpConnection(client));
//					t.start();
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
