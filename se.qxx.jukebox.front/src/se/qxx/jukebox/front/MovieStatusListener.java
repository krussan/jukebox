package se.qxx.jukebox.front;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public interface MovieStatusListener {
	public void stop();
	public void play(String filename);
}
