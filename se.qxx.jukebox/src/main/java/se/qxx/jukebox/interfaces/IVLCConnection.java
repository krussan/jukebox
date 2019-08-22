package se.qxx.jukebox.interfaces;

public interface IVLCConnection {

	/**
	 * Enqueues a file on the playlist
	 * @param filename	The MRL of the file to enqueue
	 */
	void enqueue(String filename);

	/**
	 * Toggles fullscreen
	 */
	void toggleFullscreen();

	/**
	 * Enqueues a file on the playlist
	 * @param filename		The MRL of the file to enqueue
	 * @param subFiles		A list of MLR's to subfiles to be used
	 */
	void enqueue(String filename, String subFile);

	/**
	 * Stops playback
	 */
	void stopPlayback();

	/**
	 * Pauses playback
	 */
	void pausePlayback();

	/*'
	 * Clears the playlist
	 */
	void clearPlaylist();

	/**
	 * Sets the movie playback to a specific point in the file
	 * @param seconds	The number of seconds to move to
	 */
	void seek(int seconds);

	/**
	 * Toggles VRatio output
	 */
	void toggleVRatio();

	/**
	 * Sets the current subtitle track
	 * @param subtitleID	The ID of the subtitle
	 */
	void setSubtitle(int subtitleID);

	/**
	 * Gets the current playback position
	 * @return	The number of seconds since start of playback
	 */
	String getTime();

	/**
	 * Determines whether a movie is playing
	 * @return	True if a movie is playing. False otherwise.
	 */
	boolean isPlaying();

	/**
	 * Gets the title (filename) of the current active movie
	 * @return 		The filename of the current active movie
	 */
	String getTitle();

	boolean testConnection();

	boolean isConnected();

}