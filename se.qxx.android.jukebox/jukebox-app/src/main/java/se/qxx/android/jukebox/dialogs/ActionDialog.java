package se.qxx.android.jukebox.dialogs;

import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ActionDialog implements OnClickListener{
	int id;
	RequestType reqType;
	Activity activity;
	
	final String[] commands = {
		"Blacklist",
		"Toggle watched",
		"Re-identify"
	};
	
	public ActionDialog(Activity activity, int id, RequestType requestType) {
		this.reqType = requestType;
		this.id = id;
		this.activity = activity;
	}
	
	public void show() {
		AlertDialog.Builder b = new AlertDialog.Builder(this.activity);
		b.setItems(commands, this);
		
		b.show();		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		final int id = this.id;
		final RequestType requestType = this.reqType;
		final int choice = which;		
		
		switch (choice) {
		case 0:
			final JukeboxConnectionHandler jh1 = new JukeboxConnectionHandler(
					JukeboxSettings.get().getServerIpAddress(),
					JukeboxSettings.get().getServerPort(),
					JukeboxConnectionProgressDialog.build(this.activity, "Blacklisting..."));
			
			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					jh1.blacklist(id, requestType);			
				}
			});
			t1.start();
			break;
		case 1:
			final JukeboxConnectionHandler jh2 = new JukeboxConnectionHandler(
					JukeboxSettings.get().getServerIpAddress(), 
					JukeboxSettings.get().getServerPort(),
					JukeboxConnectionProgressDialog.build(this.activity, "Toggling watched status..."));
			Thread t2 = new Thread(new Runnable() {
				@Override
				public void run() {
					jh2.toggleWatched(id, requestType);				
				}
			});			
			t2.start();
			break;
		case 2:
			final JukeboxConnectionHandler jh3 = new JukeboxConnectionHandler(
					JukeboxSettings.get().getServerIpAddress(), 
					JukeboxSettings.get().getServerPort(),
					JukeboxConnectionProgressDialog.build(this.activity, "Marking object for re-identify..."));
			Thread t3 = new Thread(new Runnable() {
				@Override
				public void run() {
					jh3.reIdentify(id, requestType);				
				}
			});			
			t3.start();
			break;
		}	
		
	}
}
