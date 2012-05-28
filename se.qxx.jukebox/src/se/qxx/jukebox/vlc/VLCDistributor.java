package se.qxx.jukebox.vlc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog.VlcPath;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Vlc.Server;
import se.qxx.jukebox.settings.Settings;

public class VLCDistributor {

	private VLCDistributor _instance;
	private Hashtable<String, VLCConnection> connectors;
	
	private VLCDistributor() {
		this.connectors = new Hashtable<String, VLCConnection>();
		
	}
	
	public VLCDistributor get() {
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
		Server s = findServerInSettings(hostName);
		Movie m = DB.getMovie(title);
		
		String filepath = m.getFilepath();
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			if (filepath.startsWith(c.getPath())) {
				VlcPath vlcPath = findVlcPath(c, hostName);
				if (vlcPath != null) {
					String filename = filepath.replace(c.getPath(), vlcPath.getPath()) + "/" + m.getFilename();
					conn.enqueue(filename);
					return true;
				}
			}
		}
		
		//conn.enqueue(filename);
		
		return false;
	}

	private VlcPath findVlcPath(Catalog c, String hostName) {
		for (VlcPath p : c.getVlcPath()){
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
