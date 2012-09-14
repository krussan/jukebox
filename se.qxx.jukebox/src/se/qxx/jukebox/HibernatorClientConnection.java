package se.qxx.jukebox;

public class HibernatorClientConnection extends TcpClient {
		
	public HibernatorClientConnection(String host, int port) 
	{
		super("Hibernator", host, port);
	}
	
	public void suspend() {
		try {
			this.sendCommand("suspend;");
		}
		catch (Exception e) {
			Log.Error("Error while suspending computer.", Log.LogType.COMM, e);
		}
	}
	
	public void hibernate() {
		try {
			this.sendCommand("hibernate;");
		}
		catch (Exception e) {
			Log.Error("Error while suspending computer.", Log.LogType.COMM, e);
		}
	}	
}
