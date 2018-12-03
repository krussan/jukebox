package se.qxx.android.jukebox.activities.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.SessionManager;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.widgets.Seeker;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.jukebox.widgets.UpdateSeekIndicator;
import se.qxx.android.tools.Logger;

public class RemotePlayerFragment extends PlayerFragment implements SeekerListener {
    private boolean loadingVisible;
    private MediaController mcontroller ;
    private Seeker seeker;

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
        return false;
    }


    private void initializeSessionManager() {
        SessionManager sessionManager = CastContext.getSharedInstance().getSessionManager();

        if (sessionManager != null) {
            sessionManager.addSessionManagerListener(this);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        if (this.isManualSeeking)
            updateSeekbarText(progress, seekBar.getMax());
    }

    private void updateSeekbarText(long progress, long duration) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);

        runOnUiThread(new UpdateSeekIndicator(progress, duration, tv));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.isManualSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int seconds = seekBar.getProgress();

        Logger.Log().d("Request --- Seek");
        castProvider.seekTo(seconds);

        this.isManualSeeking = false;
    }

    @Override
    public void updateSeeker(final long seconds, final long duration) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = findViewById(R.id.seekBarDuration);

        final long actualDuration = duration == 0 ? seekBar.getMax() : duration;

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds, actualDuration, tv, seekBar));

    }

    @Override
    public void increaseSeeker(int advanceSeconds) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = findViewById(R.id.seekBarDuration);
        int seconds = seekBar.getProgress();

        if (!this.isManualSeeking)
            getActivity().runOnUiThread(new UpdateSeekIndicator(seconds + advanceSeconds, seekBar.getMax(), tv, seekBar));
    }

    @Override
    public void setDuration(int seconds) {
        SeekBar sb = getView().findViewById(R.id.seekBarDuration);
        if (sb != null && sb.getMax() != seconds)
            sb.setMax(seconds);
    }

    @Override
    public void initializeSeeker() {
        seeker = new Seeker(this);
    }

    @Override
    public void startSeekerTimer() {
        seeker.start();
    }

    @Override
    public void stopSeekerTimer() {
        seeker.stop();
    }


    //endregion


}
