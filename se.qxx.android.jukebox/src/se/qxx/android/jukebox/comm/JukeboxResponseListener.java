package se.qxx.android.jukebox.comm;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;

public interface JukeboxResponseListener {
	public void onResponseReceived(JukeboxResponse resp);
}
