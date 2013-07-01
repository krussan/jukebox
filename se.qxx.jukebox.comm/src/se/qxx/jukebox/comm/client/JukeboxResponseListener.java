package se.qxx.jukebox.comm.client;

public interface JukeboxResponseListener {
	public void onRequestComplete(JukeboxConnectionMessage message);
}
