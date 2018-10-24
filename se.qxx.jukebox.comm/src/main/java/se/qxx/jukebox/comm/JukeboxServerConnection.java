package se.qxx.jukebox.comm;

import java.net.Socket;


public abstract class JukeboxServerConnection implements Runnable {
	
	private Socket client;
	
	protected enum LogLevel {
		Debug,
		Error,
		Critical,
		Info
	}
	
	public JukeboxServerConnection(Socket socket) {
		this.setClient(socket);
	}
	
	protected abstract void Log(LogLevel level, String message);
	protected abstract void Log(LogLevel level, String message,  Exception e);

	public Socket getClient() {
		return client;
	}

	public void setClient(Socket client) {
		this.client = client;
	}
	
	
}
