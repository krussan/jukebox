package se.qxx.jukebox.vlc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.TcpClient;

/**
 * Connection to a VLC player initialized with the RC interface
 * @author Chris
 *
 */
public class VLCConnection extends TcpClient {
		
	/**
	 * Initializes a new connection to a VLC player. The VLC player has to be initiated with the RC interface.
	 * I.e. vlc -I rc --rc-host ip:port
	 * @param host	The IP number of the host to connect to
	 * @param port	The port number of the host to connect to
	 */
	public VLCConnection(String host, int port) {
		super("VLC", host, port);
	}
		
	/**
	 * Enqueues a file on the playlist
	 * @param filename	The MRL of the file to enqueue
	 */
	public void enqueue(String filename) {
		enqueue(filename, new ArrayList<String>());
	}
	
	/**
	 * Toggles fullscreen
	 */
	public void toggleFullscreen() {
		try {
			this.sendCommand("fullscreen\n");
			this.readResponseLine();
		}
		catch (Exception e) {
			Log.Error("Error while setting fullscreen mode.", Log.LogType.COMM, e);
		}
	}
	
	/**
	 * Enqueues a file on the playlist
	 * @param filename		The MRL of the file to enqueue
	 * @param subFiles		A list of MLR's to subfiles to be used
	 */
	public void enqueue(String filename, List<String> subFiles) {
		//add file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4 :sub-file=file://Y:/Videos/Repo Men.srt
		String output = String.format("add %s", filename);
		for (String subFile : subFiles) {
			output += String.format(" :sub-file=%s", subFile);
		}
		output += "\n";

		try {
			this.sendCommand(output);
			this.readResponseLines(2);
		} catch (Exception e) {
			Log.Error("Error while adding file to playlist", Log.LogType.COMM, e);
		}
	}
	
	/**
	 * Stops playback
	 */
	public void stopPlayback() {
		try {
			this.sendCommand("stop\n");
			this.readResponseLine();
		} catch (Exception e) {
			Log.Error("Error while stopping movie", Log.LogType.COMM, e);
		}
	}
	
	/**
	 * Pauses playback
	 */
	public void pausePlayback() {
		try {
			this.sendCommand("pause\n");
			this.readResponseLine();
		} catch (Exception e) {
			Log.Error("Error while pausing movie", Log.LogType.COMM, e);
		}
	}
	
	/*'
	 * Clears the playlist
	 */
	public void clearPlaylist() {
		try {
			this.sendCommand("clear\n");
			this.readResponseLine();
		} catch (Exception e) {
			Log.Error("Error while clearing playlist", Log.LogType.COMM, e);
		}
	}

	/**
	 * Sets the movie playback to a specific point in the file
	 * @param seconds	The number of seconds to move to
	 */
	public void seek(int seconds) {
		try {
			this.sendCommand(String.format("seek %s\n", seconds));
			this.readResponseLine();
		} catch (Exception e) {
			Log.Error("Error while seeking in file", Log.LogType.COMM, e);
		}	
	}
	
	
	/**
	 * Toggles VRatio output
	 */
	public void toggleVRatio() {
		try {
			this.sendCommand("vratio\n");
			this.readResponseLine();
		} catch (Exception e) {
			Log.Error("Error while setting vertical ratio", Log.LogType.COMM, e);
		}	
	}
	
	/**
	 * Sets the current subtitle track
	 * @param subtitleID	The ID of the subtitle
	 */
	public void setSubtitle(int subtitleID) {
		try {
			this.sendCommand(String.format("strack %s\n", subtitleID));
			this.readResponseLine();
		} catch (Exception e) {
			Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
		}
	}
	
	/**
	 * Gets the current playback position
	 * @return	The number of seconds since start of playback
	 */
	public String getTime() {
		try {
			this.sendCommand("get_time\n");
			String response = this.readResponseLine();
			
			return response;
		} catch (Exception e) {
			Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
		}		
		
		return StringUtils.EMPTY;
	}

	/**
	 * Determines whether a movie is playing
	 * @return	True if a movie is playing. False otherwise.
	 */
	public boolean isPlaying() {
		try {
			this.sendCommand("is_playing\n");
			String response = StringUtils.trim(this.readResponseLine());
			
			if (response.equals("0"))
				return false;
			else
				return true;
		} catch (Exception e) {
			Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
		}		
		
		return false;
	}

	/**
	 * Gets the title (filename) of the current active movie
	 * @return 		The filename of the current active movie
	 */
	public String getTitle() {
		try {
			this.sendCommand("get_title\n");
			String response = StringUtils.trim(this.readResponseLine());
			
			return response;
		} catch (Exception e) {
			Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
		}		
		
		return StringUtils.EMPTY;
	}	
}
