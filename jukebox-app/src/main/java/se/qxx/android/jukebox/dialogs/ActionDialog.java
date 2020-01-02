package se.qxx.android.jukebox.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;

public class ActionDialog implements OnClickListener{
	long id;
	RequestType reqType;
	Activity activity;
    Resources res;
    private int mediaId;
    private JukeboxSettings settings;
	private JukeboxConnectionHandler connectionHandler;

    public ActionDialog(Activity activity, long id, int mediaId, RequestType requestType, JukeboxConnectionHandler connectionHandler) {
		this.reqType = requestType;
		this.id = id;
		this.activity = activity;
        this.res = activity.getResources();
        this.mediaId = mediaId;
        this.settings = new JukeboxSettings(activity);
        this.connectionHandler = connectionHandler;
    }
	
	public void show() {
		AlertDialog.Builder b = new AlertDialog.Builder(this.activity);
		b.setItems(res.getStringArray(R.array.actionDialogMenu), this);
		
		b.show();		
	}

	@Override
	public void onClick(DialogInterface dialog, int choice) {
        Resources res = this.activity.getResources();

		// Removed threads as the connectionHandler is threader itself
		switch (choice) {
		case 0:
			this.connectionHandler.blacklist((int) this.id, this.reqType, response -> {});
			break;
		case 1:
			this.connectionHandler.toggleWatched((int) this.id, this.reqType, response -> {});
			break;
		case 2:
			this.connectionHandler.reIdentify((int) this.id, this.reqType, response -> {});
			break;
		case 3:
			this.connectionHandler.reenlistSub((int) this.id, this.reqType, response -> {});
			break;
		case 4:
			this.connectionHandler.forceconversion(this.mediaId, response -> {});
			break;
		}
	}
}
