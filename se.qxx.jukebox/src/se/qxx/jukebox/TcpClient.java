package se.qxx.jukebox;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpClient {
	String host;
	int port;
	String clientName;
	
	Socket _sock;
	
	public TcpClient(String clientName, String host, int port) {
		this.host = host;
		this.port = port;
		this.clientName = clientName;
		
		connect();		
	}
	
	public boolean isConnected() {
		if (_sock == null)
			return false;
		else
			return _sock.isConnected();
	}
	
	private void connect() {
		try {
			Log.Debug(String.format("Connecting to %s at %s port %s", this.clientName, this.host, this.port), Log.LogType.COMM);
			_sock = new Socket(this.host, this.port);
			Log.Debug("Connected...", Log.LogType.COMM);
		} catch (Exception e) {
			Log.Error(String.format("Unable to connect to %s host :: %s port :: %s", this.clientName, this.host, this.port), Log.LogType.COMM, e);
		}
	}

	public void reconnect() {
		if (!this.isConnected())
			this.connect();
	}

	public void disconnect() {
		try {
			_sock.close();
		} catch (IOException e) {
			
			Log.Error(String.format("Disconnect failed to %s on host :: %s port :: %s", this.clientName, this.host, this.port), Log.LogType.COMM, e);
		}
	}
	
	protected void sendCommand(String command) throws IOException {
		DataOutputStream out = new DataOutputStream(_sock.getOutputStream());

		out.writeBytes(command);
		out.flush();
	}
	
}
