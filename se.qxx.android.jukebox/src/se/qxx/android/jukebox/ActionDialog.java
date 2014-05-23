package se.qxx.android.jukebox;

import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.JukeboxConnectionProgressDialog;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ActionDialog implements OnClickListener{
	Movie currentMovie;
	Activity activity;
	
	final String[] commands = {
		"Blacklist",
		"Toggle watched"
	};
	
	public ActionDialog(Activity activity, Movie movie) {
		this.currentMovie = movie;
		this.activity = activity;
	}
	
	public void show() {
		AlertDialog.Builder b = new AlertDialog.Builder(this.activity);
		b.setItems(commands, this);
		
		b.show();		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		final Movie m = this.currentMovie;
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
					jh1.blacklist(m);			
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
					jh2.toggleWatched(m);				
				}
			});			
			t2.start();
			break;
		}				
	}
}
