package se.qxx.android.jukebox.comm;

import android.os.Message;

public interface JukeboxResponseListener {
	public void onRequestComplete(Message message);
}
