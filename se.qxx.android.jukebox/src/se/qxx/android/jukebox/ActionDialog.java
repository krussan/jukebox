package se.qxx.android.jukebox;

import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.comm.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public class ActionDialog implements OnClickListener{
	Movie currentMovie;
	Context context;
	
	final String[] commands = {
		"Blacklist",
		"Toggle watched"
	};
	
	public ActionDialog(Context context, Movie movie) {
		this.currentMovie = movie;
		this.context = context;
	}
	
	public void show() {
		AlertDialog.Builder b = new AlertDialog.Builder(this.context);
		b.setItems(commands, this);
		
		b.show();		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		final Movie m = this.currentMovie;
		final int choice = which;
		final Context c = this.context;
		
		
		switch (choice) {
		case 0:
			final JukeboxConnectionHandler jh1 = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(c, "Blacklisting..."));
			
			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					jh1.blacklist(m);			
				}
			});
			t1.start();
			break;
		case 1:
			final JukeboxConnectionHandler jh2 = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(c, "Toggling watched status..."));
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
