package se.qxx.android.jukebox.activities.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.protobuf.RpcCallback;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.media.VideoControllerView;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class LocalPlayerFragment extends PlayerFragment
        implements MediaPlayer.OnPreparedListener,
            VideoControllerView.MediaPlayerEventListener,
            MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnVideoSizeChangedListener,
            VideoControllerView.MediaPlayerControl {

    private static final String TAG="LocalPlayerFragment";

    private boolean loadingVisible;
    private VideoControllerView mcontroller ;

    MediaPlayer mediaPlayer = null;
    SurfaceView surfaceView = null;
    List<String> subtitleUriList;
    private int firstTextTrack = -1;

    SurfaceHolder surfaceHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.nowplaying_local, container, false);
        surfaceHolder = getSurfaceHolder(v);

        //initializeCastProvider(v, this, null, holder);
        initializeView(v);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
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
        getActivity().runOnUiThread(() -> initializeMediaController());
    }

    private SurfaceHolder getSurfaceHolder(View v) {
        final SurfaceView view = v.findViewById(R.id.surfaceview);
        SurfaceHolder holder = view.getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                surfaceView = view;
                if (mediaPlayer != null)
                    mediaPlayer.setDisplay(view.getHolder());
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "Surface destroyed");
                if (mediaPlayer != null) {
                    mediaPlayer.release();
//                    mediaPlayer.setDisplay(null);
                }
            }
        });
        return holder;
    }

    @Override
    public void onStop() {
        super.onStop();

        //TODO: Save the media ID and position from media player
        stop();
    }


    @Override
    public void onMediaPlayerStop() {
        stop();

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
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {

        return response -> {
            if (response != null) {
                subtitleUriList = response.getSubtitleUrisList();

                Uri uri = Uri.parse(response.getUri());

                mediaPlayer = MediaPlayer.create(getContext(), uri);

                if (mediaPlayer != null) {
                    setupMediaPlayer();
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

                    mediaPlayer.setDisplay(surfaceHolder);
                    setViewLayoutRatio();

                    mediaPlayer.start();

                }
            }
        };

    }


    public void setupMediaPlayer() {
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void setViewLayoutRatio() {
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
            mediaPlayer.seekTo(position, SEEK_CLOSEST);
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
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
