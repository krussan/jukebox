package se.qxx.jukebox.comm.client;

public interface JukeboxResponseListener {
	void onRequestComplete(JukeboxConnectionMessage message);
}
