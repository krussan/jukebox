package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.media.MediaPlayer;

import com.google.protobuf.RpcCallback;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

class LocalCastProvider extends CastProvider {

    MediaPlayer mediaPlayer = null;

    @Override
    public void seek(int position) {

    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return null;
    }

    @Override
    public void stop() {

    }
}
