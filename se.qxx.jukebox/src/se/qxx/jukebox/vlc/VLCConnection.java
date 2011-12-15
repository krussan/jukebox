package se.qxx.jukebox.vlc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.ibex.nestedvm.JavaSourceCompiler;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;

public class VLCConnection {
	String _host;
	int _port;
	
	Socket _sock;
	
	public VLCConnection() 
	{
		this._host = Settings.get().getVlc().getServer().getHost();
		this._port = Settings.get().getVlc().getServer().getPort();
		
		connect();
	}
	
	public VLCConnection(String host, int port) {
		this._host = host;
		this._port = port;
		
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
			Log.Debug(String.format("Connecting to %s port %s", this._host, this._port));
			_sock = new Socket(this._host, this._port);
			Log.Debug("Connected...");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.Error(String.format("Unable to connect to VLC host :: %s port :: %s", this._host, this._port), e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.Error(String.format("Unable to connect to VLC host :: %s port :: %s", this._host, this._port), e);
		}

	}

	public void disconnect() {
		try {
			_sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.Error(String.format("Disconnect failed on host :: %s port :: %s", this._host, this._port), e);
		}
	}
	
	public void enqueue(String filename) {
		enqueue(filename, new ArrayList<String>());
	}
	
	private void sendCommand(String command) throws IOException {
		DataOutputStream out = new DataOutputStream(_sock.getOutputStream());

		out.writeBytes(command);
		out.flush();
		
		/*InputStream is = _sock.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		byte[] buffer = new byte[4096];
		StringBuilder sb = new StringBuilder();
		
		int len;
		while ((len = is.read(buffer)) != -1) { 
        	Log.Debug(String.format("Read %s bytes from inputstream", len));
			sb.append(buffer);
        	
			for (byte b : buffer) {
				Log.Debug(String.format("Byte::%s", b));
			}
			
			
        	Log.Debug(sb.toString());
		}
		*/
		
		/*is.read(b, off, len)
		String line;
		while (in.read§)
		while ((line = in.readLine()) != null) {
			Log.Debug(String.format("Received from server :: %s", line));
		}*/	
		//Log.Debug(String.format("Received from server :: %s", line));
	}
	
	public void toggleFullscreen() {
		try {
			this.sendCommand("fullscreen\n");
		}
		catch (Exception e) {
			Log.Error("Error while setting fullscreen mode.", e);
		}
	}
	
	public void enqueue(String filename, List<String> subFiles) {
		//add file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4 :sub-file=file://Y:/Videos/Repo Men.srt
		String output = String.format("add %s", filename);
		for (String subFile : subFiles) {
			output += String.format(" :sub-file=%s", subFile);
		}
		output += "\n";

		try {
			this.sendCommand(output);
		} catch (Exception e) {
			Log.Error("Error while adding file to playlist", e);
		}
	}

}
