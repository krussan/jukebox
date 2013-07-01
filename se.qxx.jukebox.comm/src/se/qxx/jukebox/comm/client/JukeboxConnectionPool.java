package se.qxx.jukebox.comm.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;

import com.google.protobuf.RpcChannel;
import com.googlecode.protobuf.socketrpc.RpcChannels;
import com.googlecode.protobuf.socketrpc.RpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;

public class JukeboxConnectionPool {

	private static JukeboxConnectionPool instance;

	public String getServerIPaddress() {
		return serverIPaddress;
	}

	public void setServerIPaddress(String serverIPaddress) {
		this.serverIPaddress = serverIPaddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private String serverIPaddress;
	private int port;
	private JukeboxService service;
	
	public JukeboxService getNonBlockingService() {
		return service;
	}
	
	public void setNonBlockingService(JukeboxService service) {
		this.service = service;
	}	

	private JukeboxConnectionPool() {
	}
	
	public static void setup(String serverIPaddress, int port){
		JukeboxConnectionPool.get().setServerIPaddress(serverIPaddress);
		JukeboxConnectionPool.get().setPort(port);
		JukeboxConnectionPool.get().setupNonBlockingService();
	}

	public static JukeboxConnectionPool get() {
		if (instance == null)
			instance = new JukeboxConnectionPool();
		
		return instance;
	}
	
	private void setupNonBlockingService() {
		ExecutorService threadPool = Executors.newFixedThreadPool(1);
		
		RpcConnectionFactory connectionFactory = SocketRpcConnectionFactories.createRpcConnectionFactory(
				this.getServerIPaddress(), 
    			this.getPort());
    			
		RpcChannel channel = RpcChannels.newRpcChannel(connectionFactory, threadPool);
		
		this.setNonBlockingService(JukeboxService.newStub(channel));		
	}

}
