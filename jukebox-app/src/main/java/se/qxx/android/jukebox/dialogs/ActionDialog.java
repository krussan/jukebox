package se.qxx.android.jukebox.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;

public class ActionDialog implements OnClickListener {
    long id;
    RequestType reqType;
    Activity activity;
    Resources res;
    private final JukeboxDomain.Media media;
    private final JukeboxSettings settings;
    private final JukeboxConnectionHandler connectionHandler;

    public ActionDialog(Activity activity, long id, JukeboxDomain.Media md, RequestType requestType, JukeboxConnectionHandler connectionHandler) {
        this.reqType = requestType;
        this.id = id;
        this.activity = activity;
        this.res = activity.getResources();
        this.media = md;
        this.settings = new JukeboxSettings(activity);
        this.connectionHandler = connectionHandler;
    }

    private JukeboxConnectionHandler getConnectionHandler() {
        return this.connectionHandler;
    }

    public void show() {
        AlertDialog.Builder b = new AlertDialog.Builder(this.activity);
        b.setItems(res.getStringArray(R.array.actionDialogMenu), this);

        b.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int choice) {
        Resources res = this.activity.getResources();

        JukeboxConnectionHandler handler = this.getConnectionHandler();

        if (handler != null) {
            // Removed threads as the connectionHandler is threader itself
            switch (choice) {
                case 0:
                    handler.blacklist((int) this.id, this.reqType, response -> {
                    });
                    break;
                case 1:
                    handler.toggleWatched((int) this.id, this.reqType, response -> {
                    });
                    break;
                case 2:
                    handler.reIdentify((int) this.id, this.reqType, response -> {
                    });
                    break;
                case 3:
                    handler.reenlistSub((int) this.id, this.reqType, response -> {
                    });
                    break;
                case 4:
                    if (this.media != null)
                        handler.forceconversion(this.media.getID(), response -> {
                        });
                    break;
            }
        }

    }
}
