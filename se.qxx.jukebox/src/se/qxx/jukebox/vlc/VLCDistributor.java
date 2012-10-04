package se.qxx.jukebox.vlc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.HibernatorClientConnection;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.WakeOnLan;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog.Vlcpaths.Vlcpath;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Vlc.Server;
import se.qxx.jukebox.settings.Settings;

public class VLCDistributor {

	private final int COMMAND_DELAY = 3000;
	private static VLCDistributor _instance;
	private Hashtable<String, VLCConnection> connectors;
	
	private VLCDistributor() {
		this.connectors = new Hashtable<String, VLCConnection>();
		
	}
	
	public static VLCDistributor get() {
		if (_instance == null)
			_instance = new VLCDistributor();
		
		return _instance;
	}
	
	public List<String> listPlayers() {
		List<String> list = new ArrayList<String>();
		for (Server s : Settings.get().getVlc().getServer()) {
			list.add(s.getName());
		}
		return list;
	}
	
	public boolean startMovie(String hostName, int id) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		Server vlcServer = findServerInSettings(hostName);
		String vlcSubsPath = vlcServer.getSubsPath();
		String serverSubsPath = Settings.get().getSubFinders().getSubsPath();
		Movie m = DB.getMovie(id);
		
		if (m != null) {
			String filepath = m.getFilepath();
			for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
				Log.Debug(String.format("Comparing %s with %s", c.getPath(), filepath), Log.LogType.COMM);
				if (filepath.startsWith(c.getPath())) {
					
					Vlcpath vlcPath = findVlcPath(c, hostName);
					if (vlcPath != null) {
						String filename = filepath.replace(c.getPath(), vlcPath.getPath()) + "/" + m.getFilename();
						
						List<Subtitle> subtitles = DB.getSubtitles(id);
						List<String> subFiles = new ArrayList<String>();
						
						for(Subtitle sub : subtitles)
							subFiles.add(sub.getFilename().replace(serverSubsPath, vlcSubsPath));
						
						conn.enqueue(filename, subFiles);
						
						return true;
					}
					else {
						Log.Debug("Couldn't find vlc path in catalog", Log.LogType.COMM);
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
	
	public boolean stopMovie(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.stopPlayback();
		
		try {
			Thread.sleep(COMMAND_DELAY);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

		conn.clearPlaylist();
		return true;
	}

	public boolean pauseMovie(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		VLCConnection conn = findConnection(hostName);
		conn.pausePlayback();
		
		return true; 
	}
	
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

	private se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog.Vlcpaths.Vlcpath findVlcPath(Catalog c, String hostName) {
		Log.Debug(String.format("Finding %s in %s", hostName, c.getPath()), Log.LogType.COMM);
		Log.Debug(String.format("Number of vlc's :: %s", c.getVlcpaths().getVlcpath().size()), Log.LogType.COMM);
		for (Vlcpath p : c.getVlcpaths().getVlcpath()){
			Log.Debug(String.format("vlc player name :: %s", p.getPlayer()), Log.LogType.COMM);	
			if (p.getPlayer().equals(hostName)) 
				return p;
		}
		return null;
	}

	private boolean assertLiveConnection(String hostName) throws VLCConnectionNotFoundException {
		VLCConnection conn = findConnection(hostName);
		if (!conn.isConnected())
			conn.reconnect();
		
		return conn.isConnected();
	}

	private VLCConnection findConnection(String hostName) throws VLCConnectionNotFoundException {
		VLCConnection conn = this.connectors.get(hostName);
		
		if (conn == null) {
			Server s = findServerInSettings(hostName);
			conn = new VLCConnection(s.getHost(), s.getPort());
		}
		
		return conn;	
	}
	
	
	private Server findServerInSettings(String hostName) throws VLCConnectionNotFoundException {
		for (Server s : Settings.get().getVlc().getServer()) {
			if (s.getName().equals(hostName)) 
				return s;
		}
		
		throw new VLCConnectionNotFoundException(hostName);

	}
	
}
