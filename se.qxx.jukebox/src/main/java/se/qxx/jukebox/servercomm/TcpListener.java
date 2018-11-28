package se.qxx.jukebox.servercomm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ITcpListener;

@Singleton
public class TcpListener implements ITcpListener {

	private RpcServer server;
	private JukeboxRpcServerConnection serverConnection;
	private IJukeboxLogger log;
	private IExecutor executor;
	
	@Inject
	public TcpListener(
			IExecutor executor, 
			LoggerFactory loggerFactory,
			JukeboxRpcServerConnection conn) {
		
		this.setExecutor(executor);
		this.setServerConnection(conn);
		this.setLog(loggerFactory.create(LogType.COMM));

	}
	
	public IExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(IExecutor executor) {
		this.executor = executor;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
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

	@Override
	public RpcServer getServer() {
		return server;
	}

	public void setServer(RpcServer server) {
		this.server = server;
	}

	@Override
	public void initialize(int port) {
		
  
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
	public Runnable getRunnable() {
		return this.getServer().getServerRunnable();
	}

	@Override
	public void initialize(ISettings settings) {
		int port = settings.getSettings().getTcpListener().getPort().getValue();
		
		initialize(port);
	}
}
