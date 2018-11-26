package se.qxx.jukebox.servercomm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;

import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ITcpListener;

@Singleton
public class TcpListener extends JukeboxThread implements ITcpListener {

	private RpcServer server;
	private ISettings settings;
	private JukeboxRpcServerConnection serverConnection;
	
	@Inject
	public TcpListener(
			IExecutor executor, 
			ISettings settings, 
			LoggerFactory loggerFactory,
			JukeboxRpcServerConnection conn) {
		
		super("TcpListener", 3000, loggerFactory.create(LogType.COMM), executor);
		this.setSettings(settings);
		this.setServerConnection(conn);
	}
	
	public JukeboxRpcServerConnection getServerConnection() {
		return serverConnection;
	}

	public void setServerConnection(JukeboxRpcServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	public JukeboxService getService() {
		return this.getServerConnection();
	}

	public ISettings getSettings() {
		return settings;
	}
	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public RpcServer getServer() {
		return server;
	}

	public void setServer(RpcServer server) {
		this.server = server;
	}


	@Override
	protected void initialize() {
		int port = this.getSettings().getSettings().getTcpListener().getPort().getValue();
  
		this.getLog().Info(String.format("Starting up RPC server. Listening on port %s",  port));
		
		ServerRpcConnectionFactory rpcConnectionFactory = 
				SocketRpcConnectionFactories
				.createServerRpcConnectionFactory(port);
		
		RpcServer server = new RpcServer(rpcConnectionFactory
				, this.getExecutor().getExecutorService()
				, true);
		
		server.registerService(this.getService());
		this.setServer(server);
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
		this.getServer().shutDown();
		super.end();
	}

	@Override
	public Runnable getRunnable() {
		return this.getServer().getServerRunnable();
	}
}
