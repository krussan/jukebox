package se.qxx.android.jukebox.activities.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.NowPlayingRemoteActivity;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;

public class LocalPlayerFragment extends PlayerFragment implements MediaPlayer.OnPreparedListener {
    private boolean loadingVisible;
    private MediaController mcontroller ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.nowplaying_local, container, false);
        initializeCastProvider(this, null);
        initializeView(v);

        return v;
    }

    private void initializeView(View v) {
        try {
            SeekBar sb = v.findViewById(R.id.seekBarDuration);
            sb.setOnSeekBarChangeListener(this);
            sb.setVisibility(View.VISIBLE);

            getConnectionHandler().getItem(
                this.getID(),
                this.getRequestType(),
                false,
                true,
                    response -> initializeView(this.getRequestType(), response));


        } catch (Exception e) {
            Logger.Log().e("Unable to initialize NowPlayingRemoteActivity", e);
        }
    }

    @Override
    public boolean isLocalPlayer() {
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isLocalPlayer()) {
            mcontroller.setMediaPlayer(castProvider);

            mcontroller.setAnchorView(findViewById(R.id.surfaceview));
            mcontroller.setEnabled(true);

            new Handler().post(() -> mcontroller.show());
        }
    }


    private void initializeMediaController() {
        boolean surfaceViewVisible = isLocalPlayer();
        setVisibility(surfaceViewVisible);

        if (surfaceViewVisible) {
            mcontroller = new MediaController(this);
            mcontroller.setMediaPlayer(castProvider);
        }
    }



}
