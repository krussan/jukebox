package se.qxx.jukebox.servercomm;

import se.qxx.jukebox.JukeboxThread;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.comm.JukeboxRpcServer;
import se.qxx.jukebox.settings.Settings;

public class TcpListener extends JukeboxThread {

	JukeboxRpcServer server;
	
	public JukeboxRpcServer getServer() {
		return server;
	}

	public void setServer(JukeboxRpcServer server) {
		this.server = server;
	}

	public TcpListener() {
		super("TcpListener", 3000, LogType.COMM);
	}

	@Override
	protected void initialize() {
		int port = Settings.get().getTcpListener().getPort().getValue();
		this.setServer(new JukeboxRpcServer(port));
  
		Log.Info(String.format("Starting up RPC server. Listening on port %s",  port), LogType.COMM);
		
		try {
			this.getServer().runServer(JukeboxRpcServerConnection.class);
		} catch (InstantiationException | IllegalAccessException e) {
			Log.Error("Error occured when starting up RPC server", LogType.COMM, e);
		}
	}
	
	@Override
	protected void execute() {
	}
	
	@Override
	public int getJukeboxPriority() {
		return 3;
	}
	
	@Override
	public void end() {
		this.getServer().stopServer();
		super.end();
	}
}
