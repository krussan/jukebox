package se.qxx.jukebox.servercomm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.factories.JukeboxRpcServerFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ITcpListener;

public class TcpListener implements ITcpListener {

	private RpcServer server;
	private JukeboxRpcServerConnection serverConnection;
	private IJukeboxLogger log;
	private IExecutor executor;
	private JukeboxRpcServerFactory rpcFactory;
	private int port;
	
	@Inject
	public TcpListener(
			IExecutor executor, 
			LoggerFactory loggerFactory,
			JukeboxRpcServerFactory rpcFactory,
			@Assisted("webserver") IStreamingWebServer webServer,
			@Assisted("port") int port) {
		
		this.setPort(port);
		this.setServerConnection((JukeboxRpcServerConnection)rpcFactory.create(webServer));
		this.setRpcFactory(rpcFactory);
		this.setExecutor(executor);
		this.setLog(loggerFactory.create(LogType.COMM));

	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public JukeboxRpcServerFactory getRpcFactory() {
		return rpcFactory;
	}

	public void setRpcFactory(JukeboxRpcServerFactory rpcFactory) {
		this.rpcFactory = rpcFactory;
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
	public void initialize() {
		
  
		this.getLog().Info(String.format("Starting up RPC server. Listening on port %s",  this.getPort()));
		
		ServerRpcConnectionFactory rpcConnectionFactory = 
				SocketRpcConnectionFactories
				.createServerRpcConnectionFactory(getPort());
		
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
}
