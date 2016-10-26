package se.qxx.jukebox.comm;

import java.util.concurrent.Executors;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;

import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;


public class JukeboxRpcServer {
	
	private int port;
	private int threadPoolSize = 20;
	
	private JukeboxService service;
	private RpcServer server;
	
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public JukeboxService getService() {
		return service;
	}
	public void setService(JukeboxService service) {
		this.service = service;
	}

	public RpcServer getServer() {
		return server;
	}
	public void setServer(RpcServer server) {
		this.server = server;
	}
	
	public JukeboxRpcServer(int port) {
		this.setPort(port);
	}
	
	public JukeboxRpcServer(int port, int threadPoolSize) {
		this.setPort(port);
		this.setThreadPoolSize(threadPoolSize);
	}

	public void runServer(Class<? extends JukeboxService> connection) throws InstantiationException, IllegalAccessException {
		ServerRpcConnectionFactory rpcConnectionFactory = 
				SocketRpcConnectionFactories
				.createServerRpcConnectionFactory(this.getPort());
		
		RpcServer server = new RpcServer(rpcConnectionFactory
				, Executors.newFixedThreadPool(this.getThreadPoolSize())
				, true);
				
		this.setServer(server);
		this.setService(connection.newInstance());
		
		server.registerService(this.getService());
		
		server.run();
	}
	
	public boolean isRunning() {
		return this.getServer().isRunning();
	}
	
	public void stopServer() {
		this.getServer().shutDown();
	}
	
	public Runnable getRunnable() {
		return this.getServer().getServerRunnable();
	}
}