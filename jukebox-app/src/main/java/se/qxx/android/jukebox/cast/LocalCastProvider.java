package se.qxx.android.jukebox.cast;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;

import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

class LocalCastProvider extends CastProvider implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "LocalCastProvider";
    MediaPlayer mediaPlayer = null;
    SurfaceView surfaceView = null;
    List<String> subtitleUriList;

    @Override
    public void initialize() {
        // set landscape mode for local playback
        //this.getParentContext().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return response -> {
            if (response != null) {
                subtitleUriList = response.getSubtitleUrisList();

                Context context = getParentContext();
                Uri uri = Uri.parse(response.getUri());

                mediaPlayer = MediaPlayer.create(context, uri);
                if (mediaPlayer != null) {
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
        if (mediaPlayer != null)
            mediaPlayer.setDisplay(view.getHolder());
    }

    @Override
    public void surfaceDestroyed() {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(null);
        }
    }

    @Override
    public void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri) {
        for (int i=0;i<mediaPlayer.getTrackInfo().length;i++) {
            MediaPlayer.TrackInfo info = mediaPlayer.getTrackInfo()[i];
            
            if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT)
                    //&& StringUtils.equalsIgnoreCase(info., subtitleUri.getUrl())) {
                mediaPlayer.selectTrack(i);
            }
        }
    }

    public void setViewLayoutRatio() {
        if (mediaPlayer != null && surfaceView != null) {
            //Get the dimensions of the video
            final int videoWidth = mediaPlayer.getVideoWidth();
            final int videoHeight = mediaPlayer.getVideoHeight();

            //Get the width of the screen
            final int screenWidth = GUITools.getDisplayMetrics(getParentContext()).widthPixels;

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
