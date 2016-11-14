package se.qxx.jukebox;

import se.qxx.jukebox.Log.LogType;
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
  
		Log.Info(String.format("Starting up RPC server. Listening on port %s",  port), LogType.COMM);
		try {
			server.runServer(JukeboxRpcServerConnection.class);
		
			while (isRunning) {
				try {
					Thread.sleep(3000);
	//					Socket client = socket.accept();
	//					
	//					Thread t = new Thread(new TcpConnection(client));
	//					t.start();
				} catch (InterruptedException e) {
					Log.Error("RPC service interrupted", LogType.COMM, e);
					this.isRunning = false;
				}				
			}
		} catch (InstantiationException | IllegalAccessException ex) {
			Log.Error("Error occured when starting up RPC server", LogType.COMM, ex);
		}

		Log.Info("Stopping RPC server", LogType.COMM);
		server.stopServer();
	}
	
	public void stopListening() {
		Log.Debug("RPC Server: stop listening called", LogType.COMM);
		this.isRunning = false;
	}

	
	
}
