package se.qxx.android.jukebox;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

public abstract class JukeboxActivityBase extends Activity {
	
	protected abstract View getRootView();
	
	protected void sendCommand(String message, JukeboxRequestType type) {
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", message);

       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), type);
       	Thread t = new Thread(h);
       	t.start();				
	}	  
}
