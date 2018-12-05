package se.qxx.android.jukebox.activities.fragments;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.media.VideoControllerView;
import se.qxx.android.tools.Logger;

public class LocalPlayerFragment extends PlayerFragment implements MediaPlayer.OnPreparedListener {
    private static final String TAG="LocalPlayerFragment";

    private boolean loadingVisible;
    private VideoControllerView mcontroller ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.nowplaying_local, container, false);
        SurfaceHolder holder = getSurfaceHolder(v);

        initializeCastProvider(v, this, null, holder);
        initializeView(v);

        return v;
    }


    private void initializeView(View v) {
        try {
            v.setOnTouchListener((view, event) -> {
                mcontroller.show();

                return false;
            });

        } catch (Exception e) {
            Logger.Log().e("Unable to initialize NowPlayingActivity", e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mcontroller.setMediaPlayer(this.getCastProvider());

        mcontroller.setAnchorView(getView().findViewById(R.id.videoSurfaceContainer));
        mcontroller.setEnabled(true);

        new Handler().post(() -> mcontroller.show());
    }


    private void initializeMediaController() {
        mcontroller = new VideoControllerView(getContext());
        mcontroller.setMediaPlayer(this.getCastProvider());
    }


    public void setVisibility(View v) {
        SurfaceView sv = v.findViewById(R.id.surfaceview);
        ProgressBar spinner = v.findViewById(R.id.spinner);

        boolean boolLoadingVisible = this.getLoadingVisible();

        int mediaControllerVisible = boolLoadingVisible ? View.GONE : View.VISIBLE;
        int spinnerVisible = boolLoadingVisible ? View.VISIBLE : View.GONE;

        sv.setVisibility(mediaControllerVisible);

        spinner.setVisibility(spinnerVisible);
    }

    @Override
    public void onGetItemCompleted() {
        getActivity().runOnUiThread(() -> initializeMediaController());
    }


    private SurfaceHolder getSurfaceHolder(View v) {
        final SurfaceView view = v.findViewById(R.id.surfaceview);
        SurfaceHolder holder = view.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                getCastProvider().surfaceCreated(view);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "Surface destroyed");
                getCastProvider().surfaceDestroyed();
            }
        });
        return holder;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getCastProvider() != null)
            getCastProvider().stop();
    }


}
