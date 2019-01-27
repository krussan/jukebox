package se.qxx.android.jukebox.activities.fragments;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import se.qxx.android.jukebox.cast.CastProvider;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChromecastPlayerFragment extends RemotePlayerFragment
        implements RemoteMediaClient.ProgressListener {

    RemoteMediaClient mRemoteMediaClient;



    public ChromecastPlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRemoteMediaClient = ChromeCastConfiguration.getRemoteMediaClient(this.getContext());

        return super.onCreateView(inflater, container, savedInstanceState);
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
    public void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri) {


        int id = getMediaTrackID(client, subtitleUri);
        if (id >= 0)
            client.setActiveMediaTracks(new long[] {(long) id});
    }

    @Override
    public void start() {
        mRemoteMediaClient.play();
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {
        if (mRemoteMediaClient != null)
            mRemoteMediaClient.stop();
    }

    @Override
    public int getDuration() {
        if (mRemoteMediaClient == null)
            return 0;
        else
            return (int)mRemoteMediaClient.getStreamDuration();
    }

    @Override
    public int getCurrentPosition() {
        if (mRemoteMediaClient == null)
            return 0;
        else
            return (int)mRemoteMediaClient.getApproximateStreamPosition();
    }


    @Override
    public void seekTo(int position) {
        if (mRemoteMediaClient != null) {
            mRemoteMediaClient.seek(position * 1000);
        }

    }

    @Override
    public boolean isPlaying() {
        if (mRemoteMediaClient == null)
            return false;

        return mRemoteMediaClient.isPlaying();
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
        return true;
    }

    @Override
    public void toggleFullScreen() {

    }

    public void startCastVideo(final String title, final String movieUrl, final List<String> subtitleUris, final List<JukeboxDomain.Subtitle> subs, final String mimeType) {
        // Since this could be called from a callback we need to trigger it
        // on the main thread.

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(getContext().getMainLooper());
        final Context mAppContext = getContext().getApplicationContext();
        final RemoteMediaClient.ProgressListener listener = this;

        Runnable myRunnable = () -> {
            if (mRemoteMediaClient != null) {
                mRemoteMediaClient.addProgressListener(listener, 300);

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

                mRemoteMediaClient.load(mi, mlo)
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

        };
        mainHandler.post(myRunnable);

    }

    @Override
    public void onProgressUpdated(long currentPosition, long duration) {
        updateSeeker((int)currentPosition / 1000, (int)duration / 1000);
    }

    private void setupCastListener() {
        mRemoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                super.onStatusUpdated();

                if (mRemoteMediaClient == null) {
                    return;
                }
                MediaStatus mediaStatus = mRemoteMediaClient.getMediaStatus();
                if (mediaStatus == null) {
                    return;
                }

                Logger.Log().d(String.format("MEDIALOAD -- playerState -- %s", mRemoteMediaClient.getPlayerState()));
                Logger.Log().d(String.format("MEDIALOAD -- idleReason -- %s", mRemoteMediaClient.getIdleReason()));

            }
        });

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
                                , this.getCastProviderMode() == CastProvider.CastProviderMode.Movie ? "" : "epi"
                                , id
                        ));

                md.addImage(new WebImage(imageUri));
            } catch (MalformedURLException e) {
                Logger.Log().e("Could not load image", e);
            }

        }
    }


    private int getMediaTrackID(RemoteMediaClient client, JukeboxDomain.SubtitleUri subtitleUri) {
        List<MediaTrack> tracks = client.getMediaInfo().getMediaTracks();
        for (int i = 0; i< tracks.size(); i++) {
            if (tracks.get(0).getContentId() == subtitleUri.getUrl())
                return i;
        }

        return -1;
    }

    @NonNull
    private long[] getActiveTracks(List<String> subtitleUris) {
        if (subtitleUris.size() > 0)
            return new long[] {1};
        else
            return new long[] {};
    }


}
