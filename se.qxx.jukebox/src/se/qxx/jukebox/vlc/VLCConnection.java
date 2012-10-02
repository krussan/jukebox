package se.qxx.jukebox.vlc;

import java.util.ArrayList;
import java.util.List;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.TcpClient;

public class VLCConnection extends TcpClient {
		
	public VLCConnection(String host, int port) {
		super("VLC", host, port);
	}
		
	public void enqueue(String filename) {
		enqueue(filename, new ArrayList<String>());
	}
	
	public void stop() {
		
	}
	
	public void toggleFullscreen() {
		try {
			this.sendCommand("fullscreen\n");
		}
		catch (Exception e) {
			Log.Error("Error while setting fullscreen mode.", Log.LogType.COMM, e);
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
			Log.Error("Error while adding file to playlist", Log.LogType.COMM, e);
		}
	}
	
	public void stopPlayback() {
		try {
			this.sendCommand("stop\n");
		} catch (Exception e) {
			Log.Error("Error while stopping movie", Log.LogType.COMM, e);
		}
	}
	
	public void pausePlayback() {
		try {
			this.sendCommand("pause\n");
		} catch (Exception e) {
			Log.Error("Error while pausing movie", Log.LogType.COMM, e);
		}
	}
	
	public void clearPlaylist() {
		try {
			this.sendCommand("clear\n");
		} catch (Exception e) {
			Log.Error("Error while clearing playlist", Log.LogType.COMM, e);
		}
	}

	public void seek(int seconds) {
		try {
			this.sendCommand(String.format("seek %s\n", seconds));
		} catch (Exception e) {
			Log.Error("Error while seeking in file", Log.LogType.COMM, e);
		}	
	}
	
	public void toggleVRatio() {
		try {
			this.sendCommand("vratio\n");
		} catch (Exception e) {
			Log.Error("Error while setting vertical ratio", Log.LogType.COMM, e);
		}	
	}
	
	public void setSubtitle(int subtitleID) {
		try {
			this.sendCommand(String.format("strack %s\n", subtitleID));
		} catch (Exception e) {
			Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
		}
	}
	
}
