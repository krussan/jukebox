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
		JukeboxConnectionHandler jh;
		switch (which) {
		case 0:
			
			jh = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(this.context, "Blacklisting..."));
			jh.blacklist(this.currentMovie);			
			
			break;
		case 1:
			jh = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(this.context, "Toggling watched status..."));
			jh.toggleWatched(this.currentMovie);			
						
			break;
		}
	}
}
