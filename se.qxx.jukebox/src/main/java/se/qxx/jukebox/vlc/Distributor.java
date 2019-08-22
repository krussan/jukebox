package se.qxx.jukebox.vlc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.factories.VLCConnectionFactory;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IVLCConnection;
import se.qxx.jukebox.interfaces.IWakeOnLan;
import se.qxx.jukebox.servercomm.HibernatorClientConnection;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog.LocalPaths.Path;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Players.Server;

@Singleton
public class Distributor implements IDistributor {
	private ReentrantLock lock = new ReentrantLock();
	
	private Hashtable<String, IVLCConnection> connectors;

	private ISettings settings;
	private IJukeboxLogger log;
	private IWakeOnLan wakeOnLan;

	private VLCConnectionFactory vlcConnectionFactory;
	
	@Inject
	public Distributor(ISettings settings, LoggerFactory loggerFactory, IWakeOnLan wakeOnLan, VLCConnectionFactory vlcConnectionFactory) {
		this.setVlcConnectionFactory(vlcConnectionFactory);
		this.setWakeOnLan(wakeOnLan);
		this.connectors = new Hashtable<String, IVLCConnection>();
		this.setSettings(settings);
		this.setLog(loggerFactory.create(LogType.COMM));
	}
	
	public VLCConnectionFactory getVlcConnectionFactory() {
		return vlcConnectionFactory;
	}

	public void setVlcConnectionFactory(VLCConnectionFactory vlcConnectionFactory) {
		this.vlcConnectionFactory = vlcConnectionFactory;
	}

	public IWakeOnLan getWakeOnLan() {
		return wakeOnLan;
	}

	public void setWakeOnLan(IWakeOnLan wakeOnLan) {
		this.wakeOnLan = wakeOnLan;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#listPlayers()
	 */
	@Override
	public List<String> listPlayers() {
		List<String> list = new ArrayList<String>();
		for (Server s : this.getSettings().getSettings().getPlayers().getServer()) {
			list.add(s.getName());
		}
		return list;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#startMovie(java.lang.String, se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public boolean startMovie(String hostName, Media md) throws VLCConnectionNotFoundException {
		return startMovie(hostName, md, StringUtils.EMPTY);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#startMovie(java.lang.String, se.qxx.jukebox.domain.JukeboxDomain.Media, java.lang.String)
	 */
	@Override
	public boolean startMovie(String hostName, Media md, String subFilename) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		Server vlcServer = findServerInSettings(hostName);
		String vlcSubsPath = vlcServer.getSubsPath();
		String serverSubsPath = this.getSettings().getSettings().getSubFinders().getSubsPath();
		
		if (md != null) {
			String filepath = md.getFilepath();
			for (Catalog c : this.getSettings().getSettings().getCatalogs().getCatalog()) {
				this.getLog().Debug(String.format("Comparing %s with %s", c.getPath(), filepath));
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
						this.getLog().Debug("Couldn't find vlc path in catalog");
					}
				}
			}
			this.getLog().Debug("Couldn't find filepath in settings");
		}
		else {
			this.getLog().Debug("Movie was not found in database");
		}
		
		//conn.enqueue(filename);
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#stopMovie(java.lang.String)
	 */
	@Override
	public boolean stopMovie(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		conn.stopPlayback();
		
		
		conn.clearPlaylist();
		return true;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#pauseMovie(java.lang.String)
	 */
	@Override
	public boolean pauseMovie(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		conn.pausePlayback();
		
		return true; 
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#toggleFullscreen(java.lang.String)
	 */
	@Override
	public boolean toggleFullscreen(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		conn.toggleFullscreen();
		
		return true; 
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#seek(java.lang.String, int)
	 */
	@Override
	public boolean seek(String hostName, int seconds) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		conn.seek(seconds);
		
		return true; 
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#toggleVRatio(java.lang.String)
	 */
	@Override
	public boolean toggleVRatio(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		conn.toggleVRatio();
		
		return true; 
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#setSubtitle(java.lang.String, int)
	 */
	@Override
	public boolean setSubtitle(String hostName, int subtitleID) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		conn.setSubtitle(subtitleID);
		
		return true; 
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#restartWithSubtitle(java.lang.String, se.qxx.jukebox.domain.JukeboxDomain.Media, java.lang.String, boolean)
	 */
	@Override
	public boolean restartWithSubtitle(String hostName, Media md, String subFilename, boolean restartAtSamePosition) throws VLCConnectionNotFoundException {
		//get current time
		String currentTime = getTime(hostName);
		int seconds = Integer.parseInt(currentTime);
		
		//stop current playback
		stopMovie(hostName);
		
		//clear playlist
		clearPlaylist(hostName);
		
		//add movie
		startMovie(hostName, md, subFilename);
		
		//seek
		try {
			Thread.sleep(1500);
			
			if (restartAtSamePosition)
				seek(hostName, seconds);
		} catch (InterruptedException e) {
			this.getLog().Error("Error occured when waiting to seek", e);
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#clearPlaylist(java.lang.String)
	 */
	@Override
	public void clearPlaylist(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return;
		
		IVLCConnection conn = findConnection(hostName);
		conn.clearPlaylist();
		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#getTime(java.lang.String)
	 */
	@Override
	public String getTime(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return StringUtils.EMPTY;
		
		IVLCConnection conn = findConnection(hostName);
		
		return conn.getTime(); 
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#isPlaying(java.lang.String)
	 */
	@Override
	public boolean isPlaying(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return false;
		
		IVLCConnection conn = findConnection(hostName);
		return conn.isPlaying();
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#getTitle(java.lang.String)
	 */
	@Override
	public String getTitle(String hostName) throws VLCConnectionNotFoundException {
		if (!assertLiveConnection(hostName))
			return StringUtils.EMPTY;
		
		IVLCConnection conn = findConnection(hostName);
		return conn.getTitle();
	}
	
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#wakeup(java.lang.String)
	 */
	@Override
	public boolean wakeup(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		
		try {
		if (s != null) {
			wakeOnLan.sendPacket(s.getBroadcastAddress(), s.getMacAddress());
			this.getLog().Info("Wake-on-LAN packet sent");
			return true;
		}
		}
		catch (IOException e) {
			this.getLog().Error("Error when sending wakeup packet", e);
			return false;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#suspend(java.lang.String)
	 */
	@Override
	public boolean suspend(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		try {
			if (s!=null) {				
				HibernatorClientConnection c = new HibernatorClientConnection(s.getHost(), s.getHibernatorPort(), this.getLog());
				c.suspend();
				c.disconnect();
				return true;
			}
		}
		catch (Exception e)
		{
			this.getLog().Error("Error when sending suspend command", e);			
			return false;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.vlc.IDistributor#hibernate(java.lang.String)
	 */
	@Override
	public boolean hibernate(String hostName) throws VLCConnectionNotFoundException {
		Server s = findServerInSettings(hostName);
		try {
			if (s!=null) {
				HibernatorClientConnection c = new HibernatorClientConnection(s.getHost(), s.getHibernatorPort(), getLog());
				c.hibernate();
				c.disconnect();
				return true;
			}
		}
		catch (Exception e)
		{
			this.getLog().Error("Error when sending suspend command", e);			
			return false;
		}
		
		return false;
	}	

	private Path findLocalPath(Catalog c, String hostName) {
		this.getLog().Debug(String.format("Finding %s in %s", hostName, c.getPath()));
		this.getLog().Debug(String.format("Number of vlc's :: %s", c.getLocalPaths().getPath().size()));
		for (Path p : c.getLocalPaths().getPath()){
			this.getLog().Debug(String.format("vlc player name :: %s", p.getPlayer()));	
			if (p.getPlayer().equals(hostName)) 
				return p;
		}
		return null;
	}

	private boolean assertLiveConnection(String hostName) throws VLCConnectionNotFoundException {
		lock.lock();
		try {
			IVLCConnection conn = findConnection(hostName);
			
			if (!conn.isConnected() || !conn.testConnection())
				conn = createNewConnection(hostName);

			return conn.isConnected();				
		}
		finally {
			lock.unlock();
		}
	}

	private IVLCConnection findConnection(String hostName) throws VLCConnectionNotFoundException {
		lock.lock();
		try {
			IVLCConnection conn = this.connectors.get(hostName);
			
			if (conn == null)
				conn = createNewConnection(hostName);
			
			return conn;				
		}
		finally {
			lock.unlock();
		}

	}
	
	private IVLCConnection createNewConnection(String hostName) throws VLCConnectionNotFoundException {
		lock.lock();
		try {
			Server s = findServerInSettings(hostName);
			
			if (this.connectors.containsKey(hostName))
				this.connectors.remove(hostName);
			
			IVLCConnection conn = this.getVlcConnectionFactory().create(s.getHost(), s.getPort());
			
			this.connectors.put(hostName, conn);
			return conn;			
		}
		finally {
			lock.unlock();
		}

	}
	
	
	private Server findServerInSettings(String hostName) throws VLCConnectionNotFoundException {
		for (Server s : this.getSettings().getSettings().getPlayers().getServer()) {
			if (s.getName().equals(hostName)) 
				return s;
		}
		
		throw new VLCConnectionNotFoundException(hostName);

	}
	
}
