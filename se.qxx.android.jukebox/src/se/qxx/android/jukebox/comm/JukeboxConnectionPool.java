package se.qxx.android.jukebox.comm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;

import com.google.protobuf.RpcChannel;
import com.googlecode.protobuf.socketrpc.RpcChannels;
import com.googlecode.protobuf.socketrpc.RpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;

public class JukeboxConnectionPool {

	private static JukeboxConnectionPool instance;

	private JukeboxService service;
	
	public JukeboxService getNonBlockingService() {
		return service;
	}
	
	public void setNonBlockingService(JukeboxService service) {
		this.service = service;
	}	

	private JukeboxConnectionPool() {
		setupNonBlockingService();
	}

	public static JukeboxConnectionPool get() {
		if (instance == null)
			instance = new JukeboxConnectionPool();
		
		return instance;
	}
	
	private void setupNonBlockingService() {
		ExecutorService threadPool = Executors.newFixedThreadPool(1);
		
		RpcConnectionFactory connectionFactory = SocketRpcConnectionFactories.createRpcConnectionFactory(
    			JukeboxSettings.get().getServerIpAddress(), 
    			JukeboxSettings.get().getServerPort());
    			
		RpcChannel channel = RpcChannels.newRpcChannel(connectionFactory, threadPool);
		
		this.setNonBlockingService(JukeboxService.newStub(channel));		
	}

}
