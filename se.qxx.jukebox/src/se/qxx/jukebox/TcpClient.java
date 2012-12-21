package se.qxx.jukebox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;

public class TcpClient {
	String host;
	int port;
	String clientName;
	int readTimeout = 0;
	
	Socket _sock;
	
	public TcpClient(String clientName, String host, int port, int readTimeout) {
		this.host = host;
		this.port = port;
		this.clientName = clientName;
		this.readTimeout = readTimeout;
		
		connect();		
	}

	
	public boolean isConnected() {
		if (_sock == null)
			return false;
		else 
			return _sock.isConnected() && !_sock.isClosed() && !_sock.isOutputShutdown() && !_sock.isOutputShutdown();
	}
	
	private void connect() {
		try {
			Log.Debug(String.format("Connecting to %s at %s port %s", this.clientName, this.host, this.port), Log.LogType.COMM);
			_sock = new Socket(this.host, this.port);
			_sock.setSoTimeout(this.readTimeout);
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
		
//		String response = readResponse();
		
	}
	
//	private String readResponse() throws IOException {
//		byte[] data = new byte[512];
//		int bytesRead, offset = 0;
//		
//		InputStream inp = _sock.getInputStream();
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		File f = new File("tcpclient.out");
//		FileOutputStream fos = new FileOutputStream(f, true);
//		
//		int available = inp.available();
//		
//		while (available > 0 && (bytesRead = inp.read(data, 0, data.length)) != -1) {
//			offset += bytesRead;
//			bos.write(data, offset, bytesRead);
//			fos.write(data, offset, bytesRead);			
//		}		
//		
//		fos.flush();
//		bos.flush();
//
//		String response = bos.toString("ISO-8859-1");
//		
//		bos.close();
//		fos.close();
//		
//		return response;
//	}

	protected String readResponseLine() throws IOException {
		return readResponseLines(1);
	}
	
	protected String readResponseLines(int nrOfLines) throws IOException {
		InputStream ins = _sock.getInputStream();
		InputStreamReader isr = new InputStreamReader(ins, "ISO-8859-1");
		BufferedReader r = new BufferedReader(isr);
		
		String line = StringUtils.EMPTY;
		
		for (int i = 0; i<nrOfLines;i++) {
			line = r.readLine();
			Log.Debug(String.format("Response was :: %s", line), LogType.COMM);			
		}
		
		return line;
	}
	
}
