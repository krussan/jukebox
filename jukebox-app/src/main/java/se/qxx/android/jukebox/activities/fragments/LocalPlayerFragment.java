package se.qxx.android.jukebox.activities.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.media.VideoControllerView;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class LocalPlayerFragment extends PlayerFragment
        implements MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerEventListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener,
        VideoControllerView.MediaPlayerControl {

    private static final String TAG = "LocalPlayerFragment";

    private boolean loadingVisible;
    private VideoControllerView mcontroller;

    MediaPlayer mediaPlayer = null;
    List<String> subtitleUriList;
    private int firstTextTrack = -1;

    CountDownLatch surfaceAquired = new CountDownLatch(1);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.nowplaying_local, container, false);

        initializeSurfaceHolder(v);
        initializeView(v);
        initializeMediaPlayer();

        return v;
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(mp -> mp.release());

        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnVideoSizeChangedListener(this);

        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startMedia();
    }

    private void initializeView(View v) {
        try {
            v.setOnTouchListener((view, event) -> {
                view.performClick();
                mcontroller.show();

                return false;
            });

        } catch (Exception e) {
            Logger.Log().e("Unable to initialize NowPlayingActivity", e);
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mcontroller.setMediaPlayer(this);
        mcontroller.setEventListener(this);

        mcontroller.setAnchorView(getView().findViewById(R.id.videoSurfaceContainer));
        mcontroller.setEnabled(true);

        new Handler().post(() -> mcontroller.show());
    }


    private void initializeMediaController() {
        mcontroller = new VideoControllerView(getContext());
        mcontroller.setMediaPlayer(this);
    }


    public void setVisibility(View v) {
        VideoView sv = v.findViewById(R.id.surfaceview);
        ProgressBar spinner = v.findViewById(R.id.spinner);

        boolean boolLoadingVisible = this.getLoadingVisible();

        int mediaControllerVisible = boolLoadingVisible ? View.GONE : View.VISIBLE;
        int spinnerVisible = boolLoadingVisible ? View.VISIBLE : View.GONE;

        sv.setVisibility(mediaControllerVisible);

        spinner.setVisibility(spinnerVisible);
    }

    @Override
    public void onGetItemCompleted() {
        getActivity().runOnUiThread(this::initializeMediaController);
    }

    private void initializeSurfaceHolder(View v) {
        SurfaceView view = v.findViewById(R.id.surfaceview);
        SurfaceHolder holder = view.getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                if (mediaPlayer != null)
                    mediaPlayer.setDisplay(surfaceHolder);

                surfaceAquired.countDown();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "Surface destroyed");
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.setDisplay(null);
                    }
                }
                catch (IllegalStateException stateEx) {
                    Logger.Log().e("Ignoring IllegalStateException");
                }
            }
        });
    }

    @Override
    public void onStop() {
        try {
            setExitPosition(mediaPlayer.getCurrentPosition());
            //TODO: Save the media ID and position from media player
            pause();
        }
        catch (IllegalStateException stateEx) {
            Logger.Log().e("Illegal state ignored (!)");
        }

        super.onStop();

    }


    @Override
    public void onMediaPlayerStop() {
        setExitPosition(mediaPlayer.getCurrentPosition());


        getActivity().finish();
    }

    @Override
    public void onMediaPlayerSubtitleClick() {
        //Intent i = new Intent(getActivity(), SubSelectActivity.class);
        //i.putExtra("media", this.getMedia());
        //i.putExtra("subSelectMode", SubSelectActivity.SubSelectMode.Return);
        //startActivityForResult(i, 1);
        showSubtitleDialog();
    }

    @Override
    public void onMediaPlayerFullscreen() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // was supposed to get the result from the subtitle view but it has been replaced by a dialog
//        if (requestCode == 1) {
//            if(resultCode == RESULT_OK) {
//                String strEditText = data.getStringExtra("editTextValue");
//            }
//        }
    }

    @Override
    public void SubtitleSelected(JukeboxDomain.SubtitleUri subtitleUri) {
        Log.d(TAG, String.format("Setting subtitle :: %s", subtitleUri.getSubtitle().getFilename()));

        this.setSubtitle(subtitleUri);
    }

    @Override
    public void onStartMovieComplete(JukeboxDomain.JukeboxResponseStartMovie response) {
        if (response != null) {
            subtitleUriList = response.getSubtitleUrisList();

            startMediaPlayer(response);
        }
    }

    private void startMediaPlayer(JukeboxDomain.JukeboxResponseStartMovie response) {
        try {
            surfaceAquired.await(); // be sure surface has been aquired

            if (mediaPlayer != null) {
                try {
                    Uri uri = Uri.parse(response.getUri());
                    mediaPlayer.setDataSource(this.getContext(), uri);
                    mediaPlayer.prepare();

                    setupSubtitles(response);
                    setViewLayoutRatio();

                    mediaPlayer.start();


                } catch (IOException e) {
                    Logger.Log().e(String.format("Error when preparing movie URI :: %s", response.getUri()));
                }
            }
        } catch (InterruptedException e) {
            Logger.Log().e("Lock interrupted (!)");
        }
    }


    private void setupSubtitles(JukeboxDomain.JukeboxResponseStartMovie response) {
        firstTextTrack = mediaPlayer.getTrackInfo().length;

        for (String subUri : response.getSubtitleUrisList()) {
            try {
                mediaPlayer.addTimedTextSource(
                        getContext(),
                        Uri.parse(subUri),
                        MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
            } catch (IOException e) {
                Logger.Log().e("Unable to add substitle", e);
            }
        }

        if (response.getSubtitleUrisCount() > 0)
            mediaPlayer.selectTrack(firstTextTrack);
    }


    public void setViewLayoutRatio() {
        SurfaceView surfaceView = getView().findViewById(R.id.surfaceview);

        if (mediaPlayer != null && surfaceView != null) {
            //Get the dimensions of the video
            final int videoWidth = mediaPlayer.getVideoWidth();
            final int videoHeight = mediaPlayer.getVideoHeight();

            //Get the width of the screen
            final int screenWidth = GUITools.getDisplayMetrics(getActivity()).widthPixels;

            getActivity().runOnUiThread(() -> {
                //Get the SurfaceView layout parameters
                ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();

                //Set the width of the SurfaceView to the width of the screen
                lp.width = screenWidth;

                //Set the height of the SurfaceView to match the aspect ratio of the video
                //be sure to cast these as floats otherwise the calculation will likely be 0
                lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);

                //Commit the layout parameters
                surfaceView.setLayoutParams(lp);
            });
        }
    }


    @Override
    public void start() {
        if (mediaPlayer != null)
            mediaPlayer.start();
        else
            Log.d(TAG, "Mediaplayer is null on start");
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {

        if (mediaPlayer != null)
            return mediaPlayer.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        else
            return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    public void stop() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
        catch (IllegalStateException stateEx) {
            Logger.Log().e("Illegal state ignored (!)");
        }

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {

    }

    @Override
    public void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri) {
        int index = Collections.binarySearch(subtitleUriList, subtitleUri.getUrl());

        if (index >= 0 && firstTextTrack >= 0) {
            mediaPlayer.selectTrack(firstTextTrack + index);
        }

    }

}
