package se.qxx.jukebox.vlc;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.interfaces.IVLCConnection;
import se.qxx.jukebox.servercomm.TcpClient;

/**
 * Connection to a VLC player initialized with the RC interface
 * @author Chris
 *
 */
public class VLCConnection extends TcpClient implements IVLCConnection {
	private static String mutex = "MUTEX";
	private final static int COMMAND_TIMEOUT = 3000;
	private IUtils utils;

	
	/**
	 * Initializes a new connection to a VLC player. The VLC player has to be initiated with the RC interface.
	 * I.e. vlc -I oldrc --rc-host ip:port
	 * @param host	The IP number of the host to connect to
	 * @param port	The port number of the host to connect to
	 */
	@Inject
	public VLCConnection(@Assisted String host, @Assisted int port, LoggerFactory loggerFactory, IUtils utils) {
		super("VLC", host, port, COMMAND_TIMEOUT, loggerFactory.create(LogType.COMM));
		this.setUtils(utils);
	}
		
	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#enqueue(java.lang.String)
	 */
	@Override
	public void enqueue(String filename) {
		synchronized (mutex) {
			enqueue(filename, StringUtils.EMPTY);
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#toggleFullscreen()
	 */
	@Override
	public void toggleFullscreen() {
		synchronized (mutex) {
			try {
				this.sendCommand("fullscreen\n");
			}
			catch (Exception e) {
				this.getLog().Error("Error while setting fullscreen mode.", e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#enqueue(java.lang.String, java.lang.String)
	 */
	@Override
	public void enqueue(String filename, String subFile) {
		//add file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4 :sub-file=file://Y:/Videos/Repo Men.srt
		synchronized (mutex) {
			String output = String.format("add %s", filename);
				
			if (!StringUtils.isEmpty(subFile)) 
				output += String.format(" :sub-file=%s", subFile);

			output += "\n";
	
			this.getLog().Debug(String.format("Sending enqueue command:: %s", output));
			try {
				this.sendCommand(output);
				this.readLinesUntilFound("add:\\sreturned.*");
				
			} catch (Exception e) {
				this.getLog().Error("Error while adding file to playlist", e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#stopPlayback()
	 */
	@Override
	public void stopPlayback() {
		synchronized (mutex) {
			try {
				this.sendCommand("stop\n");
				this.readLinesUntilFound("stop:\\sreturned.*");
			} catch (Exception e) {
				this.getLog().Error("Error while stopping movie", e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#pausePlayback()
	 */
	@Override
	public void pausePlayback() {
		synchronized (mutex) {
			try {
				this.sendCommand("pause\n");
				this.readLinesUntilFound("pause:\\sreturned.*");
			} catch (Exception e) {
				this.getLog().Error("Error while pausing movie", e);
			}
		}
	}
	
	/*'
	 * Clears the playlist
	 */
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#clearPlaylist()
	 */
	@Override
	public void clearPlaylist() {
		synchronized (mutex) {
			try {
				this.sendCommand("clear\n");
				this.readLinesUntilFound("clear:\\sreturned.*");
			} catch (Exception e) {
				this.getLog().Error("Error while clearing playlist", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#seek(int)
	 */
	@Override
	public void seek(int seconds) {
		synchronized (mutex) {
			try {
				this.sendCommand(String.format("seek %s\n", seconds));
				this.readLinesUntilFound("seek:\\sreturned.*");
			} catch (Exception e) {
				this.getLog().Error("Error while seeking in file", e);
			}	
		}
	}
	
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#toggleVRatio()
	 */
	@Override
	public void toggleVRatio() {
		synchronized (mutex) {
			try {
				this.sendCommand("vratio\n");
				this.readResponseLine();
			} catch (Exception e) {
				this.getLog().Error("Error while setting vertical ratio", e);
			}	
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#setSubtitle(int)
	 */
	@Override
	public void setSubtitle(int subtitleID) {
		synchronized (mutex) {
			try {
				this.getLog().Debug(String.format("Setting subtitle track %s", subtitleID));
				this.sendCommand(String.format("strack %s\n", subtitleID));
				this.readResponseLine();
			} catch (Exception e) {
				this.getLog().Error("Error while setting subtitle track", e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#getTime()
	 */
	@Override
	public String getTime() {
		synchronized (mutex) {
			try {
				this.sendCommand("get_time\n");
				String response = this.readNextLineIgnoringStatusChanges();
				
				return response;
			} catch (Exception e) {
				this.getLog().Error("Error while setting subtitle track", e);
			}		
		
			return "0";
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#isPlaying()
	 */
	@Override
	public boolean isPlaying() {
		synchronized (mutex) {
			try {
				this.sendCommand("is_playing\n");
				String response = StringUtils.trim(this.readNextLineIgnoringStatusChanges());
				
				if (response.equals("0"))
					return false;
				else
					return true;
			} catch (IOException e) {
				this.getLog().Error("Error while setting subtitle track", e);
			}
			
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#getTitle()
	 */
	@Override
	public String getTitle() {
		synchronized (mutex) {
			try {
				this.sendCommand("get_title\n");
				String response = StringUtils.trim(this.readNextLineIgnoringStatusChanges());
				
				return response;
			} catch (Exception e) {
				this.getLog().Error("Error while setting subtitle track", e);
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
		long startTime = this.getUtils().getCurrentTimestamp();
		String line = StringUtils.EMPTY;

		this.getLog().Debug(String.format("Waiting for response like :: %s", pattern));
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		while (!found && this.getUtils().getCurrentTimestamp() - startTime <= COMMAND_TIMEOUT) {
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
		long startTime = this.getUtils().getCurrentTimestamp();
		String line = StringUtils.EMPTY;
		
		Pattern p = Pattern.compile("status\\schange:.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		while (this.getUtils().getCurrentTimestamp() - startTime <= COMMAND_TIMEOUT) {			
			line = this.readResponseLine();
			Matcher m = p.matcher(line);

			if (!m.matches())
				break;
		}
		
		return line;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IVLCConnection#testConnection()
	 */
	@Override
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
//				this.getLog().Error(String.format("VLC Connection is not live - reinitialize"), e);
//			}
//		}
//		
//		return val;
		
		return true;
	}	
}
