import java.util.concurrent.Executors;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;
import com.googlecode.protobuf.socketrpc.SocketRpcServer;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;


public class JukeboxRpcServer {
	
	private int port;
	private int threadPoolSize = 20;
	
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
	public JukeboxRpcServer(int port) {
		this.setPort(port);
	}
	
	public JukeboxRpcServer(int port, int threadPoolSize) {
		this.setPort(port);
		this.setThreadPoolSize(threadPoolSize);
	}

	public void runServer() {
		ServerRpcConnectionFactory rpcConnectionoFactory = 
				SocketRpcConnectionFactories
				.createServerRpcConnectionFactory(this.getPort());
		
		RpcServer server = new RpcServer(rpcConnectionoFactory
				, Executors.newFixedThreadPool(this.getThreadPoolSize())
				, true);
		
		server.registerService(new JukeboxRpcServerConnection());
		server.startServer();
	}
}
