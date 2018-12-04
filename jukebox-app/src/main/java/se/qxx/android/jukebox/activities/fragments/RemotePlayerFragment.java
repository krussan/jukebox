package se.qxx.android.jukebox.activities.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.SubSelectActivity;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.widgets.Seeker;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.jukebox.widgets.UpdateSeekIndicator;
import se.qxx.android.tools.Logger;

public class RemotePlayerFragment extends PlayerFragment implements SeekerListener, SeekBar.OnSeekBarChangeListener, SessionManagerListener<Session>, View.OnClickListener {
    private boolean loadingVisible;
    private MediaController mcontroller ;
    private Seeker seeker;
    private boolean isManualSeeking = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.nowplaying_remote, container, false);
        initializeCastProvider(v,null, this, null);

        initializeView(v);
        return v;
    }

    private void initializeView(View v) {
        SeekBar sb = v.findViewById(R.id.seekBarDuration);
        sb.setOnSeekBarChangeListener(this);

        setupButtons((ViewGroup)v);
    }

    private void setupButtons(ViewGroup group) {
        for(int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if (v instanceof ViewGroup) {
                setupButtons((ViewGroup)v);
            }
            else if (v instanceof ImageButton) {
                v.setOnClickListener(this);
            }
        }
    }

    public void setVisibility(View v) {
        SeekBar sb = v.findViewById(R.id.seekBarDuration);
        LinearLayout linearLayout2 = v.findViewById(R.id.linearLayout2);
        LinearLayout linearLayout1 = v.findViewById(R.id.linearLayout1);
        LinearLayout linearLayoutButtons1 = v.findViewById(R.id.linearLayoutButtons1);
        TextView txtSeekIndicator = v.findViewById(R.id.txtSeekIndicator);
        ProgressBar spinner = v.findViewById(R.id.spinner);

        int standardControlsVisible = this.getLoadingVisible() ? View.GONE : View.VISIBLE;
        int spinnerVisible = this.getLoadingVisible() ? View.VISIBLE : View.GONE;

        sb.setVisibility(standardControlsVisible);
        linearLayout1.setVisibility(standardControlsVisible);
        linearLayout2.setVisibility(standardControlsVisible);
        linearLayoutButtons1.setVisibility(standardControlsVisible);
        txtSeekIndicator.setVisibility(standardControlsVisible);

        spinner.setVisibility(spinnerVisible);

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
        final TextView tv = getView().findViewById(R.id.txtSeekIndicator);

        getActivity().runOnUiThread(new UpdateSeekIndicator(progress, duration, tv));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.isManualSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int seconds = seekBar.getProgress();

        Logger.Log().d("Request --- Seek");
        this.getCastProvider().seekTo(seconds);

        this.isManualSeeking = false;
    }

    @Override
    public void updateSeeker(final long seconds, final long duration) {
        final TextView tv = getView().findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = getView().findViewById(R.id.seekBarDuration);

        final long actualDuration = duration == 0 ? seekBar.getMax() : duration;

        if (!this.isManualSeeking)
            getActivity().runOnUiThread(new UpdateSeekIndicator(seconds, actualDuration, tv, seekBar));

    }

    @Override
    public void increaseSeeker(int advanceSeconds) {
        View v = getView();

        if (v != null) {
            final TextView tv = v.findViewById(R.id.txtSeekIndicator);
            final SeekBar seekBar = v.findViewById(R.id.seekBarDuration);

            if (seekBar != null && tv != null) {
                int seconds = seekBar.getProgress();

                if (!this.isManualSeeking)
                    getActivity().runOnUiThread(new UpdateSeekIndicator(seconds + advanceSeconds, seekBar.getMax(), tv, seekBar));
            }
        }
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

    //region --BUTTONS--

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnPlay:
                Logger.Log().d("Request --- StartMovie");
                getCastProvider().startMovie();
                break;
            case R.id.btnFullscreen:
                Logger.Log().d("Request --- ToggleFullScreen");
                setupFullscreen();
                break;
            case R.id.btnPause:
                Logger.Log().d("Request --- Pause");
                pauseMovie();
                break;
            case R.id.btnStop:
                Logger.Log().d("Request --- StopMove");
                this.getCastProvider().stop();
                getActivity().finish();

                break;
            case R.id.btnViewInfo:
                if (StringUtils.isNotEmpty(this.getImdbUrl())) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getImdbUrl()));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(getActivity(), "No IMDB link available", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnSubSelection:
                Intent i = new Intent(getActivity(), SubSelectActivity.class);
                i.putExtra("media", this.getMediaList().get(this.getCurrentMediaIndex()));
                startActivity(i);
                break;
            default:
                break;
        }
    }

    private void pauseMovie() {
        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (ChromeCastConfiguration.isChromeCastActive()) {
            RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this.getContext());

            if (client != null) {
                client.togglePlayback();
            }
        }
        else {
            seeker.toggle();
            this.getConnectionHandler().pauseMovie(player);
        }
    }

    private void setupFullscreen() {

        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (!ChromeCastConfiguration.isChromeCastActive()) {
            this.getConnectionHandler().toggleFullscreen(player);
        }

    }


    //endregion

    @Override
    public void onSessionStarting(Session session) {
        // stop the current cast provider
        if (this.getCastProvider() != null) {
            this.getCastProvider().stop();
        }
    }

    @Override
    public void onSessionStarted(Session session, String s) {
        initializeCastProvider(getView(),null, this, null);
        // start movie and seek?
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {

    }

    @Override
    public void onSessionEnding(Session session) {
    }

    @Override
    public void onSessionEnded(Session session, int i) {
        // stop the current cast provider
        if (this.getCastProvider() != null) {
            if (this.getCastProvider().isPlaying()) {
                this.getCastProvider().stop();
                initializeCastProvider(getView(), null, this, null);
            }
        }

        // start movie and seek?
    }

    @Override
    public void onSessionResuming(Session session, String s) {

    }

    @Override
    public void onSessionResumed(Session session, boolean b) {

    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {

    }

    @Override
    public void onSessionSuspended(Session session, int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        CastContext.getSharedInstance().getSessionManager().removeSessionManagerListener(this);
    }

    @Override
    public void onGetItemCompleted() {
        getActivity().runOnUiThread(() -> initializeSessionManager());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (seeker != null)
            seeker.stop();
    }


    @Override
    public void onStop() {
        super.onStop();

        if (seeker != null)
            seeker.stop();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (seeker != null)
            seeker.start();
    }

}