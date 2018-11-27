package se.qxx.jukebox.servercomm;

import se.qxx.jukebox.interfaces.IJukeboxLogger;

public class HibernatorClientConnection extends TcpClient {
		
	public HibernatorClientConnection(String host, int port, IJukeboxLogger log) 
	{
		super("Hibernator", host, port, 0, log);
	}
	
	public void suspend() {
		try {
			this.sendCommand("suspend;");
		}
		catch (Exception e) {
			this.getLog().Error("Error while suspending computer.", e);
		}
	}
	
	public void hibernate() {
		try {
			this.sendCommand("hibernate;");
		}
		catch (Exception e) {
			this.getLog().Error("Error while suspending computer.", e);
		}
	}	
}
