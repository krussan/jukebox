package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;

public interface IDistributor {

	/**
	 * Lists the players declared in the XML file
	 * @return A list of strings with the player names
	 */
	List<String> listPlayers();

	boolean startMovie(String hostName, Media md) throws VLCConnectionNotFoundException;

	/**
	 * Starts a movie on a specific VLC playrer
	 * @param hostName 	The player on which to start the movie
	 * @param id 		The ID of the movie
	 * @return			True if the call succeeds
	 * @throws VLCConnectionNotFoundException
	 */
	boolean startMovie(String hostName, Media md, String subFilename) throws VLCConnectionNotFoundException;

	/**
	 * Stops a movie on a specific VLC player and clears the playlist
	 * @param hostName		The player on which to stop the movie
	 * @return				True if the call succeeds
	 * @throws VLCConnectionNotFoundException
	 */
	boolean stopMovie(String hostName) throws VLCConnectionNotFoundException;

	/**
	 * Pauses a movie on a specific VLC player
	 * @param hostName		The player on which to pause the movie
	 * @return				True if the call succeeds	
	 * @throws VLCConnectionNotFoundException
	 */
	boolean pauseMovie(String hostName) throws VLCConnectionNotFoundException;

	/**
	 * Toggles fullscreen mode on a specific VLC player
	 * @param hostName
	 * @return
	 * @throws VLCConnectionNotFoundException
	 */
	boolean toggleFullscreen(String hostName) throws VLCConnectionNotFoundException;

	boolean seek(String hostName, int seconds) throws VLCConnectionNotFoundException;

	boolean toggleVRatio(String hostName) throws VLCConnectionNotFoundException;

	boolean setSubtitle(String hostName, int subtitleID) throws VLCConnectionNotFoundException;

	boolean restartWithSubtitle(String hostName, Media md, String subFilename, boolean restartAtSamePosition)
			throws VLCConnectionNotFoundException;

	void clearPlaylist(String hostName) throws VLCConnectionNotFoundException;

	String getTime(String hostName) throws VLCConnectionNotFoundException;

	boolean isPlaying(String hostName) throws VLCConnectionNotFoundException;

	String getTitle(String hostName) throws VLCConnectionNotFoundException;

	boolean wakeup(String hostName) throws VLCConnectionNotFoundException;

	boolean suspend(String hostName) throws VLCConnectionNotFoundException;

	boolean hibernate(String hostName) throws VLCConnectionNotFoundException;

}