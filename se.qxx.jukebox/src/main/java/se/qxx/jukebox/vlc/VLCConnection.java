package se.qxx.jukebox.vlc;

import java.io.IOException;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.TcpClient;
import se.qxx.jukebox.Util;

/**
 * Connection to a VLC player initialized with the RC interface
 * @author Chris
 *
 */
public class VLCConnection extends TcpClient {
	private static String mutex = "MUTEX";
	private final static int COMMAND_TIMEOUT = 3000;
	
	/**
	 * Initializes a new connection to a VLC player. The VLC player has to be initiated with the RC interface.
	 * I.e. vlc -I oldrc --rc-host ip:port
	 * @param host	The IP number of the host to connect to
	 * @param port	The port number of the host to connect to
	 */
	public VLCConnection(String host, int port) {
		super("VLC", host, port, COMMAND_TIMEOUT);
	}
		
	/**
	 * Enqueues a file on the playlist
	 * @param filename	The MRL of the file to enqueue
	 */
	public void enqueue(String filename) {
		synchronized (mutex) {
			enqueue(filename, StringUtils.EMPTY);
		}
	}
	
	/**
	 * Toggles fullscreen
	 */
	public void toggleFullscreen() {
		synchronized (mutex) {
			try {
				this.sendCommand("fullscreen\n");
			}
			catch (Exception e) {
				Log.Error("Error while setting fullscreen mode.", Log.LogType.COMM, e);
			}
		}
	}
	
	/**
	 * Enqueues a file on the playlist
	 * @param filename		The MRL of the file to enqueue
	 * @param subFiles		A list of MLR's to subfiles to be used
	 */
	public void enqueue(String filename, String subFile) {
		//add file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4 :sub-file=file://Y:/Videos/Repo Men.srt
		synchronized (mutex) {
			String output = String.format("add %s", filename);
				
			if (!StringUtils.isEmpty(subFile)) 
				output += String.format(" :sub-file=%s", subFile);

			output += "\n";
	
			Log.Debug(String.format("Sending enqueue command:: %s", output), LogType.COMM);
			try {
				this.sendCommand(output);
				this.readLinesUntilFound("add:\\sreturned.*");
				
			} catch (Exception e) {
				Log.Error("Error while adding file to playlist", Log.LogType.COMM, e);
			}
		}
	}
	
	/**
	 * Stops playback
	 */
	public void stopPlayback() {
		synchronized (mutex) {
			try {
				this.sendCommand("stop\n");
				this.readLinesUntilFound("stop:\\sreturned.*");
			} catch (Exception e) {
				Log.Error("Error while stopping movie", Log.LogType.COMM, e);
			}
		}
	}
	
	/**
	 * Pauses playback
	 */
	public void pausePlayback() {
		synchronized (mutex) {
			try {
				this.sendCommand("pause\n");
				this.readLinesUntilFound("pause:\\sreturned.*");
			} catch (Exception e) {
				Log.Error("Error while pausing movie", Log.LogType.COMM, e);
			}
		}
	}
	
	/*'
	 * Clears the playlist
	 */
	public void clearPlaylist() {
		synchronized (mutex) {
			try {
				this.sendCommand("clear\n");
				this.readLinesUntilFound("clear:\\sreturned.*");
			} catch (Exception e) {
				Log.Error("Error while clearing playlist", Log.LogType.COMM, e);
			}
		}
	}

	/**
	 * Sets the movie playback to a specific point in the file
	 * @param seconds	The number of seconds to move to
	 */
	public void seek(int seconds) {
		synchronized (mutex) {
			try {
				this.sendCommand(String.format("seek %s\n", seconds));
				this.readLinesUntilFound("seek:\\sreturned.*");
			} catch (Exception e) {
				Log.Error("Error while seeking in file", Log.LogType.COMM, e);
			}	
		}
	}
	
	
	/**
	 * Toggles VRatio output
	 */
	public void toggleVRatio() {
		synchronized (mutex) {
			try {
				this.sendCommand("vratio\n");
				this.readResponseLine();
			} catch (Exception e) {
				Log.Error("Error while setting vertical ratio", Log.LogType.COMM, e);
			}	
		}
	}
	
	/**
	 * Sets the current subtitle track
	 * @param subtitleID	The ID of the subtitle
	 */
	public void setSubtitle(int subtitleID) {
		synchronized (mutex) {
			try {
				Log.Debug(String.format("Setting subtitle track %s", subtitleID), LogType.COMM);
				this.sendCommand(String.format("strack %s\n", subtitleID));
				this.readResponseLine();
			} catch (Exception e) {
				Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
			}
		}
	}
	
	/**
	 * Gets the current playback position
	 * @return	The number of seconds since start of playback
	 */
	public String getTime() {
		synchronized (mutex) {
			try {
				this.sendCommand("get_time\n");
				String response = this.readNextLineIgnoringStatusChanges();
				
				return response;
			} catch (Exception e) {
				Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
			}		
		
			return "0";
		}
	}

	/**
	 * Determines whether a movie is playing
	 * @return	True if a movie is playing. False otherwise.
	 */
	public boolean isPlaying() {
		synchronized (mutex) {
			try {
				this.sendCommand("is_playing\n");
				String response = StringUtils.trim(this.readNextLineIgnoringStatusChanges());
				
				if (response.equals("0"))
					return false;
				else
					return true;
			} catch (SocketException e) {
				Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			return false;
		}
	}

	/**
	 * Gets the title (filename) of the current active movie
	 * @return 		The filename of the current active movie
	 */
	public String getTitle() {
		synchronized (mutex) {
			try {
				this.sendCommand("get_title\n");
				String response = StringUtils.trim(this.readNextLineIgnoringStatusChanges());
				
				return response;
			} catch (Exception e) {
				Log.Error("Error while setting subtitle track", Log.LogType.COMM, e);
			}		
			
			return StringUtils.EMPTY;
		}
	}
	
	/**
	 * Reads lines from response stream until a line is found that matches a regular expression patter
	 * @param pattern	The pattern to match
	 * @return			Returns true if response was found. False if the function timed out.
	 * @throws IOException
	 */
	private boolean readLinesUntilFound(String pattern) throws IOException {
		boolean found = false;
		long startTime = Util.getCurrentTimestamp();
		String line = StringUtils.EMPTY;

		Log.Debug(String.format("Waiting for response like :: %s", pattern), LogType.COMM);
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		while (!found && Util.getCurrentTimestamp() - startTime <= COMMAND_TIMEOUT) {
			line = this.readResponseLine();
			Matcher m = p.matcher(line);

			found = m.matches();
		}
		
		return found;
	}
	
	/**
	 * Reads the next line from the response stream. If status changes are found then these are ignored.
	 * @return		The next line received
	 * @throws IOException
	 */
	private String readNextLineIgnoringStatusChanges() throws IOException {
		long startTime = Util.getCurrentTimestamp();
		String line = StringUtils.EMPTY;
		
		Pattern p = Pattern.compile("status\\schange:.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		while (Util.getCurrentTimestamp() - startTime <= COMMAND_TIMEOUT) {			
			line = this.readResponseLine();
			Matcher m = p.matcher(line);

			if (!m.matches())
				break;
		}
		
		return line;
	}

	public boolean testConnection() {
		// try something on the wire to assure live connection. Avoid broken pipe exception.
		// this is ugly but I dont have a better way at the moment
//		boolean val = false;
//
//		synchronized(mutex) {
//			try {
//				this.sendCommand("get_time\n");
//				this.readNextLineIgnoringStatusChanges();
//				
//				val = true;
//			}
//			catch (IOException e){
//				Log.Error(String.format("VLC Connection is not live - reinitialize"), LogType.COMM, e);
//			}
//		}
//		
//		return val;
		
		return true;
	}	
}
