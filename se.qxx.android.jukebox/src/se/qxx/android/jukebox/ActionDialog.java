package se.qxx.android.jukebox;

import se.qxx.android.jukebox.comm.ConnectionWrapper;
import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.AlertDialog;
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
		switch (which) {
		case 0:
			ConnectionWrapper.sendCommandWithProgressDialog(
				this.context, 
				"Blacklisting...", 
				JukeboxRequestType.BlacklistMovie, 
				this.currentMovie);
			
			break;
		case 1:
			ConnectionWrapper.sendCommandWithProgressDialog(
				this.context, 
				"Toggling watched status...", 
				JukeboxRequestType.ToggleWatched, 
				this.currentMovie);
			
			break;
		}
	}
}
