package se.qxx.jukebox.servercomm;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.JukeboxThread;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.comm.JukeboxRpcServer;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.Settings;

@Singleton
public class TcpListener extends JukeboxThread implements ITcpListener {

	private JukeboxRpcServer server;
	private ISettings settings;
	
	@Inject
	public TcpListener(IExecutor executor, ISettings settings) {
		super("TcpListener", 3000, LogType.COMM, executor);
		this.setSettings(settings);
	}
	
	public ISettings getSettings() {
		return settings;
	}
	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public JukeboxRpcServer getServer() {
		return server;
	}

	public void setServer(JukeboxRpcServer server) {
		this.server = server;
	}


	@Override
	protected void initialize() {
		int port = this.getSettings().getSettings().getTcpListener().getPort().getValue();
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

	@Override
	public Runnable getRunnable() {
		return this;
	}
}
