package se.qxx.android.jukebox.cast;

import android.app.Activity;

import com.google.protobuf.RpcCallback;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

class LocalCastProvider extends CastProvider {

    public LocalCastProvider(Activity parentContext, JukeboxConnectionHandler comm, JukeboxConnectionProgressDialog dialog, SeekerListener listener) {
        super(parentContext, comm, dialog, listener);
    }

    @Override
    public void initialize() {

    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return null;
    }
}