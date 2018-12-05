package se.qxx.android.jukebox.cast;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class ChromeCastProvider extends CastProvider implements RemoteMediaClient.ProgressListener {

    RemoteMediaClient client;

    @Override
    public void initialize() {

    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return response -> {
            JukeboxConnectionProgressDialog dialog = getDialog();
            if (dialog != null)
                dialog.close();

            if (response != null) {
                startCastVideo(
                        getTitle(),
                        response.getUri(),
                        response.getSubtitleUrisList(),
                        response.getSubtitleList(),
                        response.getMimeType());

                initializeSubtitles();
            }
        };
    }

    @Override
    public void stop() {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(
                this.getParentContext().getApplicationContext());

        if (client != null)
            client.stop();
    }

    @Override
    public boolean usesMediaController() {
        return false;
    }

    private void setupCastListener() {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this.getParentContext());
        final Context context = this.getParentContext();

        client.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                super.onStatusUpdated();

                RemoteMediaClient remoteMediaClient = ChromeCastConfiguration.getRemoteMediaClient(context);

                if (remoteMediaClient == null) {
                    return;
                }
                MediaStatus mediaStatus = remoteMediaClient.getMediaStatus();
                if (mediaStatus == null) {
                    return;
                }

                Logger.Log().d(String.format("MEDIALOAD -- playerState -- %s", remoteMediaClient.getPlayerState()));
                Logger.Log().d(String.format("MEDIALOAD -- idleReason -- %s", remoteMediaClient.getIdleReason()));

            }
        });

    }

    public void startCastVideo(final String title, final String movieUrl, final List<String> subtitleUris, final List<JukeboxDomain.Subtitle> subs, final String mimeType) {
        // Since this could be called from a callback we need to trigger it
        // on the main thread.

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(this.getParentContext().getMainLooper());
        final Context mAppContext = this.getParentContext().getApplicationContext();
        final RemoteMediaClient.ProgressListener listener = this;

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                final RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(mAppContext);

                if (client != null) {
                    client.addProgressListener(listener, 300);

                    MediaMetadata md = getMediaMetadata(title, movieUrl);

                    List<MediaTrack> tracks = getSubtitleTracks(subs, subtitleUris);
                    long[] activeTrackIds = getActiveTracks(subtitleUris);

                    TextTrackStyle style = ChromeCastConfiguration.getTextStyle();

                    MediaInfo mi = new MediaInfo.Builder(movieUrl)
                            .setMetadata(md)
                            .setMediaTracks(tracks)
                            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                            .setContentType(mimeType)
                            .setTextTrackStyle(style)
                            .build();

                    MediaLoadOptions mlo = new MediaLoadOptions.Builder()
                            .setAutoplay(true)
                            .setActiveTrackIds(activeTrackIds)
                            .build();

                    client.load(mi, mlo)
                            .setResultCallback(mediaChannelResult -> {
                                Status status = mediaChannelResult.getStatus();

                                if (status.isSuccess()) {
                                    Logger.Log().d(String.format("MEDIALOAD -- Media load success :: %s", status.getStatusMessage()));
                                }
                                else {
                                    Logger.Log().d(String.format("MEDIALOAD -- Media load FAILURE :: %s", status.getStatusMessage()));
                                }

                            });

                }

            }



        };
        mainHandler.post(myRunnable);

    }

    private List<MediaTrack> getSubtitleTracks(List<JukeboxDomain.Subtitle> subs, List<String> subtitleUris) {
        List<MediaTrack> tracks = new ArrayList<>();

        for (int i=0;i<subtitleUris.size();i++) {
            if (i<subs.size()) {
                JukeboxDomain.Subtitle currentSub = subs.get(i);

                MediaTrack subtitle = new MediaTrack.Builder(i + 1, MediaTrack.TYPE_TEXT)
                        .setContentId(subtitleUris.get(i))
                        .setContentType("text/vtt")
                        .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                        .setName(currentSub.getDescription())
                        .setLanguage("en-US")
                        .build();

                tracks.add(subtitle);

            }
        }

        return tracks;
    }

    @Override
    public void onProgressUpdated(long currentPosition, long duration) {
        if (getSeekerListener() != null)
            getSeekerListener().updateSeeker((int)currentPosition / 1000, (int)duration / 1000);
    }

    private MediaMetadata getMediaMetadata(String title, String movieUrl) {
        MediaMetadata md = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        md.putString(MediaMetadata.KEY_TITLE, title);

        //addMetadataImage(movieUrl, md);

        return md;
    }

    private void addMetadataImage(String movieUrl, MediaMetadata md) {
        int id = getID();

        if (id > 0) {

            String baseUrl = StringUtils.EMPTY;

            try {
                URL movieUri = new URL(movieUrl);
                Uri imageUri =
                        Uri.parse(String.format("%s://%s:%s/%sthumb%s"
                                , movieUri.getProtocol()
                                , movieUri.getHost()
                                , movieUri.getPort()
                                , this.getCastProviderMode() == CastProviderMode.Movie ? "" : "epi"
                                , id
                        ));

                md.addImage(new WebImage(imageUri));
            } catch (MalformedURLException e) {
                Logger.Log().e("Could not load image", e);
            }

        }
    }

    @NonNull
    private long[] getActiveTracks(List<String> subtitleUris) {
        if (subtitleUris.size() > 0)
            return new long[] {1};
        else
            return new long[] {};
    }


    @Override
    public void start() {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(
                this.getParentContext().getApplicationContext());

        client.play();
    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int position) {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(
                this.getParentContext().getApplicationContext());

        if (client != null) {
            client.seek(position * 1000);
        }

    }

    @Override
    public void surfaceCreated(SurfaceView view) {

    }

    @Override
    public void surfaceDestroyed() {

    }

    @Override
    public boolean isPlaying() {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(getParentContext());

        if (client == null)
            return false;

        return client.isPlaying();
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
        return 0;
    }
}
