package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceView;

import com.google.protobuf.RpcCallback;

import java.io.IOException;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

class LocalCastProvider extends CastProvider implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener {

    MediaPlayer mediaPlayer = null;
    SurfaceView surfaceView = null;

    @Override
    public void initialize() {
        // set landscape mode for local playback
        //this.getParentContext().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return response -> {
            if (response != null) {
                Context context = getParentContext();
                Uri uri = Uri.parse(response.getUri());

                mediaPlayer = MediaPlayer.create(context, uri);
                setupMediaPlayer();

                for (String subUri : response.getSubtitleUrisList()) {
                    try {
                        mediaPlayer.addTimedTextSource(context, Uri.parse(subUri), "text/vtt");
                    } catch (IOException e) {
                        Logger.Log().e("Unable to add substitle", e);
                    }
                }

                if (response.getSubtitleUrisCount() > 0)
                    mediaPlayer.selectTrack(1);

                mediaPlayer.setDisplay(getDisplay());
                setViewLayoutRatio();

                mediaPlayer.start();

                initializeSubtitles();
            }
        };

    }

    public void setupMediaPlayer() {
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(getOnPreparedListener());
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean usesMediaController() {
        return true;
    }


    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position * 1000);
        }
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
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
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.release();
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
    public void surfaceCreated(SurfaceView view) {
        surfaceView = view;
    }

    public void setViewLayoutRatio() {

        if (mediaPlayer != null && surfaceView != null) {
            //Get the dimensions of the video
            final int videoWidth = mediaPlayer.getVideoWidth();
            final int videoHeight = mediaPlayer.getVideoHeight();

            //Get the width of the screen
            final int screenWidth = getParentContext().getWindowManager().getDefaultDisplay().getWidth();


            getParentContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Get the SurfaceView layout parameters
                    android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();

                    //Set the width of the SurfaceView to the width of the screen
                    lp.width = screenWidth;

                    //Set the height of the SurfaceView to match the aspect ratio of the video
                    //be sure to cast these as floats otherwise the calculation will likely be 0
                    lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);

                    //Commit the layout parameters
                    surfaceView.setLayoutParams(lp);
                }
            });
        }
    }
}
