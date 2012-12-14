package se.qxx.android.jukebox;

import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.comm.JukeboxResponseListener;
import se.qxx.android.tools.ProgressDialogHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;

public abstract class JukeboxActivityBase extends Activity {
	
	protected abstract View getRootView();
	
	protected void sendCommand(String message, JukeboxRequestType type, Object... args) {
		this.sendCommand(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {}
		}, message, type, args);
	}	  
	
	protected void sendCommand(OnDismissListener listener, String message, JukeboxRequestType type, Object... args) {
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", message);

       	if (listener != null)
       		d.setOnDismissListener(listener);
       	
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), type, args);
       	Thread t = new Thread(h);
       	t.start();						
	}
	
	protected void sendCommand(final JukeboxResponseListener listener, String message, JukeboxRequestType type, Object... args) {
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(listener, type, args);
       	Thread t = new Thread(h);
       	t.start();						
	}	
}
