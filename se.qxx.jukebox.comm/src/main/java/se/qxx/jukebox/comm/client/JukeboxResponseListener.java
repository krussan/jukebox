package se.qxx.jukebox.comm.client;

public interface JukeboxResponseListener<T> {
	void onRequestComplete(JukeboxConnectionMessage message);
	void onDataReceived(T response);
}
