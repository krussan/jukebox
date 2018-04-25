package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

public class ChromeCastProvider extends CastProvider implements RemoteMediaClient.ProgressListener {
    public ChromeCastProvider(Activity parentContext, JukeboxConnectionHandler comm, JukeboxConnectionProgressDialog dialog, SeekerListener listener) {
        super(parentContext, comm, dialog, listener);
    }

    @Override
    public void initialize(String title) {
        this.setTitle(title);
    }

    @Override
    public void seek(long position) {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(
                this.getParentContext().getApplicationContext());

        if (client != null) {
            client.seek(position * 1000);
        }
    }

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return new RpcCallback<JukeboxDomain.JukeboxResponseStartMovie>() {
            @Override
            public void run(JukeboxDomain.JukeboxResponseStartMovie response) {

                getDialog().close();

                int movieID = Model.get().getCurrentMovie().getID();

                if (response != null) {
                    startCastVideo(
                            movieID,
                            getTitle(),
                            response.getUri(),
                            response.getSubtitleUrisList(),
                            response.getSubtitleList());

                }
            }
        };
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

    public void startCastVideo(final int movieId, final String title, final String movieUrl, final List<String> subtitleUris, final List<JukeboxDomain.Subtitle> subs) {
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

                    MediaMetadata md = getMediaMetadata();

                    List<MediaTrack> tracks = getSubtitleTracks();
                    long[] activeTrackIds = getActiveTracks();

                    TextTrackStyle style = ChromeCastConfiguration.getTextStyle();

                    MediaInfo mi = new MediaInfo.Builder(movieUrl)
                            .setMetadata(md)
                            .setMediaTracks(tracks)
                            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                            .setContentType("video/mp4")
                            .setTextTrackStyle(style)
                            .build();

                    MediaLoadOptions mlo = new MediaLoadOptions.Builder()
                            .setAutoplay(true)
                            .setActiveTrackIds(activeTrackIds)
                            .build();

                    client.load(mi, mlo)
                            .setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {

                                @Override
                                public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
                                    Status status = mediaChannelResult.getStatus();

                                    if (status.isSuccess()) {
                                        Logger.Log().d(String.format("MEDIALOAD -- Media load success :: %s", status.getStatusMessage()));
                                    }
                                    else {
                                        Logger.Log().d(String.format("MEDIALOAD -- Media load FAILURE :: %s", status.getStatusMessage()));
                                    }

                                }
                            });



                }

            }

            @NonNull
            private MediaMetadata getMediaMetadata() {
                MediaMetadata md = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
                md.putString(MediaMetadata.KEY_TITLE, title);

                int id = getId();

                if (id > 0) {

                    String baseUrl = StringUtils.EMPTY;

                    try {
                        URL movieUri = new URL(movieUrl);
                        Uri imageUri =
                                Uri.parse(String.format("%s://%s:%s/thumb%s"
                                        , movieUri.getProtocol()
                                        , movieUri.getHost()
                                        , movieUri.getPort()
                                        , id
                                ));

                        md.addImage(new WebImage(imageUri));
                    } catch (MalformedURLException e) {
                        Logger.Log().e("Could not load image", e);
                    }

                }

                return md;
            }

            private long[] getActiveTracks() {
                long[] activeTrackIds = null;
                if (subtitleUris.size() > 0)
                    activeTrackIds = new long[] {1};
                return activeTrackIds;
            }

            private List<MediaTrack> getSubtitleTracks() {
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
        };
        mainHandler.post(myRunnable);

    }

    @Override
    public void onProgressUpdated(long currentPosition, long duration) {
        if (getSeekerListener() != null)
            getSeekerListener().updateSeeker((int)currentPosition / 1000);
    }
}
