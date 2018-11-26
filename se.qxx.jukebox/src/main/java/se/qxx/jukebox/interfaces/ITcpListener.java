package se.qxx.jukebox.interfaces;

import com.googlecode.protobuf.socketrpc.RpcServer;

public interface ITcpListener {

	public RpcServer getServer();
	public Runnable getRunnable();
	public void initialize(int port);
	public void initialize(ISettings settings);

}