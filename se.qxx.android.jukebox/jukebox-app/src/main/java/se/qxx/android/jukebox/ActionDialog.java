package se.qxx.android.jukebox;

import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.JukeboxConnectionProgressDialog;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;

public class ActionDialog implements OnClickListener{
	int id;
	RequestType reqType;
	Activity activity;
    Resources res;
	
	public ActionDialog(Activity activity, int id, RequestType requestType) {
		this.reqType = requestType;
		this.id = id;
		this.activity = activity;
        this.res = activity.getResources();
	}
	
	public void show() {
		AlertDialog.Builder b = new AlertDialog.Builder(this.activity);
		b.setItems(res.getStringArray(R.array.actionDialogMenu), this);
		
		b.show();		
	}

	@Override
	public void onClick(DialogInterface dialog, int choice) {
        Resources res = this.activity.getResources();

		JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(),
				JukeboxSettings.get().getServerPort(),
				JukeboxConnectionProgressDialog.build(this.activity, res.getStringArray(R.array.actionDialogStrings)[choice]));

		// Removed threads as the connectionHandler is threader itself
		switch (choice) {
		case 0:
			jh.blacklist(this.id, this.reqType);
			break;
		case 1:
			jh.toggleWatched(this.id, this.reqType);
			break;
		case 2:
			jh.reIdentify(this.id, this.reqType);
			break;
		case 3:
			jh.reenlistSub(this.id, this.reqType);
			break;
		}
		
	}
}
