package se.qxx.android.jukebox.comm;

import se.qxx.android.tools.ProgressDialogHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

public class ConnectionWrapper {
		
	public static void sendCommandWithProgressDialog(Context context, String message, JukeboxRequestType type, Object... args) {
		ConnectionWrapper.sendCommandWithProgressDialog(context, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {}
		}, message, type, args);
	}	  
	
	public static void sendCommandWithProgressDialog(Context context, OnDismissListener listener, String message, JukeboxRequestType type, Object... args) {
       	ProgressDialog d = ProgressDialog.show(context, "Jukebox", message);

       	if (listener != null)
       		d.setOnDismissListener(listener);
       	
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(context, d), type, args);
       	Thread t = new Thread(h);
       	t.start();						
	}
	
	public static void sendCommandWithResponseListener(final JukeboxResponseListener listener, String message, JukeboxRequestType type, Object... args) {
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(listener, type, args);
       	Thread t = new Thread(h);
       	t.start();						
	}	

}
