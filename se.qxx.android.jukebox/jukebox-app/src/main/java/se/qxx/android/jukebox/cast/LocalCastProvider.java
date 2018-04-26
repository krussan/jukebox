package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.protobuf.RpcCallback;

import java.io.IOException;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

class LocalCastProvider extends CastProvider {

    MediaPlayer mediaPlayer = null;

    @Override
    public void initialize() {
    }

    @Override
    public void seek(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position * 1000);
        }
    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return new RpcCallback<JukeboxDomain.JukeboxResponseStartMovie>() {
            @Override
            public void run(JukeboxDomain.JukeboxResponseStartMovie response) {
                int movieID = Model.get().getCurrentMovie().getID();

                if (response != null) {
                    Context context = getParentContext();
                    Uri uri = Uri.parse(response.getUri());

                    mediaPlayer = MediaPlayer.create(context, uri);
                    for (String subUri : response.getSubtitleUrisList()) {
                        try {
                            mediaPlayer.addTimedTextSource(context, Uri.parse(subUri), "text/vtt");
                        } catch (IOException e) {
                            Logger.Log().e("Unable to add substitle", e);
                        }
                    }

                    if (response.getSubtitleUrisCount() > 0)
                        mediaPlayer.selectTrack(1);

                    mediaPlayer.start();

                    initializeSubtitles();
                }
            }
        };

    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
