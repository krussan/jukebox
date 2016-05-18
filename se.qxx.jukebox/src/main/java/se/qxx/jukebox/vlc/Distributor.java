package se.qxx.jukebox.vlc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.HibernatorClientConnection;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.WakeOnLan;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.Sorter;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog.LocalPaths.Path;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Players.Server;
import se.qxx.jukebox.settings.Settings;

public class Distributor {

	private static Distributor _instance;
	private Hashtable<String, VLCConnection> connectors;

	/**
	 * Private constructor for the VLC Distributor.
	 */
	private Distributor() {
		this.connectors = new Hashtable<String, VLCConnection>();
	}
	
	/**
	 * Public getter for the singleton VLC Distributor object
	 * @return The VLC Distributor
	 */
	public static Distributor get() {
		if (_instance == null)
			_instance = new Distributor();
		
		return _instance;
	}
	
	/**
	 * Lists the players declared in the XML file
	 * @return A list of strings with the player names
	 */
	public List<String> listPlayers() {
		List<String> list = new ArrayList<String>();
		for (Server s : Settings.get().getPlayers().getServer()) {
			list.add(s.getName());
		}
		return list;
	}
	
	public boolean startMovie(String hostName, Movie m) throws VLCConnectionNotFoundException {
		return startMovie(hostName, m, StringUtils.EMPTY);
	}
	
	/**
	 * Starts a movie on a specific VLC playrer
	 * @param hostName 	The player on which to start the movie
	 * @param id 		The ID of the movie
	 * @return			True if the call succeeds
	 * @throws VLCConnectionNotFoundException
	 */
	public boolean startMovie(String hostName, Movie m, String subFilename) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		Server vlcServer = findServerInSettings(hostName);
		String vlcSubsPath = vlcServer.getSubsPath();
		String serverSubsPath = Settings.get().getSubFinders().getSubsPath();
		
		if (m != null) {
			for (Media md : m.getMediaList()) {
				String filepath = md.getFilepath();
				for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
					Log.Debug(String.format("Comparing %s with %s", c.getPath(), filepath), Log.LogType.COMM);
					if (filepath.startsWith(c.getPath())) {
						Path vlcPath = findLocalPath(c, hostName);
						if (vlcPath != null) {
							String filename = filepath.replace(c.getPath(), vlcPath.getPath()) + "/" + md.getFilename();
							String finalSubFilename = subFilename;
							
							if (StringUtils.isEmpty(finalSubFilename)) {
								// It appears that VLC RC interface only reads the first sub-file option specified
								// in the command sent. No need to send more than one. We pick the top rated one by sorting
								// the subtitles.
								List<Subtitle> sortedSubtitles = Sorter.sortSubtitlesByRating(md.getSubsList());
								
								if (sortedSubtitles.size() > 0)
									finalSubFilename = sortedSubtitles.get(0).getFilename();
							}													

							conn.enqueue(filename, finalSubFilename.replace(serverSubsPath, vlcSubsPath));
							
							return true;
						}
						else {
							Log.Debug("Couldn't find vlc path in catalog", Log.LogType.COMM);
						}
					}
				} 
			}
			Log.Debug("Couldn't find filepath in settings", Log.LogType.COMM);
		}
		else {
			Log.Debug("Movie was not found in database", Log.LogType.COMM);
		}
		
		//conn.enqueue(filename);
		
		return false;
	}
	
	/**
	 * Stops a movie on a specific VLC player and clears the playlist
	 * @param hostName		The player on which to stop the movie
	 * @return				True if the call succeeds
	 * @throws VLCConnectionNotFoundException
	 */
	public boolean stopMovie(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.stopPlayback();
		
		
		conn.clearPlaylist();
		return true;
	}

	/**
	 * Pauses a movie on a specific VLC player
	 * @param hostName		The player on which to pause the movie
	 * @return				True if the call succeeds	
	 * @throws VLCConnectionNotFoundException
	 */
	public boolean pauseMovie(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.pausePlayback();
		
		return true; 
	}
	
	/**
	 * Toggles fullscreen mode on a specific VLC player
	 * @param hostName
	 * @return
	 * @throws VLCConnectionNotFoundException
	 */
	public boolean toggleFullscreen(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.toggleFullscreen();
		
		return true; 
	}

	public boolean seek(String hostName, int seconds) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.seek(seconds);
		
		return true; 
	}
	
	public boolean toggleVRatio(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.toggleVRatio();
		
		return true; 
	}

	public boolean setSubtitle(String hostName, int subtitleID) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.setSubtitle(subtitleID);
		
		return true; 
	}
	
	public boolean restartWithSubtitle(String hostName, Movie m, String subFilename, boolean restartAtSamePosition) throws VLCConnectionNotFoundException {
		//get current time
		String currentTime = getTime(hostName);
		int seconds = Integer.parseInt(currentTime);
		
		//stop current playback
		stopMovie(hostName);
		
		//clear playlist
		clearPlaylist(hostName);
		
		//add movie
		startMovie(hostName, m, subFilename);
		
		//seek
		try {
			Thread.sleep(1500);
			
			if (restartAtSamePosition)
				seek(hostName, seconds);
		} catch (InterruptedException e) {
			Log.Error("Error occured when waiting to seek", LogType.COMM, e);
		}
		
		return true;
	}
	
	public void clearPlaylist(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return;
		
		VLCConnection conn = findConnection(hostName);
		conn.clearPlaylist();
		
	}
	
	public String getTime(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return StringUtils.EMPTY;
		
		VLCConnection conn = findConnection(hostName);
		
		return conn.getTime(); 
	}

	public boolean isPlaying(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		return conn.isPlaying();
	}

	public String getTitle(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return StringUtils.EMPTY;
		
		VLCConnection conn = findConnection(hostName);
		return conn.getTitle();
	}
	
	
	public boolean wakeup(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		
		try {
		if (s != null) {
			WakeOnLan.sendPacket(s.getBroadcastAddress(), s.getMacAddress());
			return true;
		}
		}
		catch (IOException e) {
			Log.Error("Error when sending wakeup packet", LogType.COMM, e);
			return false;
		}
		
		return false;
	}
	
	public boolean suspend(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		try {
			if (s!=null) {
				HibernatorClientConnection c = new HibernatorClientConnection(s.getHost(), s.getHibernatorPort());
				c.suspend();
				c.disconnect();
				return true;
			}
		}
		catch (Exception e)
		{
			Log.Error("Error when sending suspend command", LogType.COMM, e);			
			return false;
		}
		
		return false;
	}
	
	public boolean hibernate(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		try {
			if (s!=null) {
				HibernatorClientConnection c = new HibernatorClientConnection(s.getHost(), s.getHibernatorPort());
				c.hibernate();
				c.disconnect();
				return true;
			}
		}
		catch (Exception e)
		{
			Log.Error("Error when sending suspend command", LogType.COMM, e);			
			return false;
		}
		
		return false;
	}	

	private Path findLocalPath(Catalog c, String hostName) {
		Log.Debug(String.format("Finding %s in %s", hostName, c.getPath()), Log.LogType.COMM);
		Log.Debug(String.format("Number of vlc's :: %s", c.getLocalPaths().getPath().size()), Log.LogType.COMM);
		for (Path p : c.getLocalPaths().getPath()){
			Log.Debug(String.format("vlc player name :: %s", p.getPlayer()), Log.LogType.COMM);	
			if (p.getPlayer().equals(hostName)) 
				return p;
		}
		return null;
	}

	private synchronized boolean assertLiveConnection(String hostName) throws VLCConnectionNotFoundException {
		VLCConnection conn = findConnection(hostName);
		
		if (!conn.isConnected() || !conn.testConnection())
			conn = createNewConnection(hostName);

		return conn.isConnected();	
	}

	private synchronized VLCConnection findConnection(String hostName) throws VLCConnectionNotFoundException {
		VLCConnection conn = this.connectors.get(hostName);
		
		if (conn == null)
			conn = createNewConnection(hostName);
		
		return conn;	
	}
	
	private synchronized VLCConnection createNewConnection(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		
		if (this.connectors.containsKey(hostName))
			this.connectors.remove(hostName);
		
		VLCConnection conn = new VLCConnection(s.getHost(), s.getPort());
		this.connectors.put(hostName, conn);
		return conn;
	}
	
	
	private Server findServerInSettings(String hostName) throws VLCConnectionNotFoundException {
		for (Server s : Settings.get().getPlayers().getServer()) {
			if (s.getName().equals(hostName)) 
				return s;
		}
		
		throw new VLCConnectionNotFoundException(hostName);

	}
	
}
