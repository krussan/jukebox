package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.protobuf.RpcCallback;

import java.io.IOException;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

class LocalCastProvider extends CastProvider implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener {

    MediaPlayer mediaPlayer = null;

    @Override
    public void initialize() {
        // set landscape mode for local playback
        //this.getParentContext().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
                    mediaPlayer.start();

                    initializeSubtitles();
                }
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
}
