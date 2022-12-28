package se.qxx.android.jukebox.activities.fragments;


import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChromecastPlayerFragment extends RemotePlayerFragment
        implements RemoteMediaClient.ProgressListener {

    RemoteMediaClient mRemoteMediaClient;

    Lock handlerLock = new ReentrantLock();
    Condition startVideoComplete = handlerLock.newCondition();

    private static final int PROGRESS_LISTENER_INTERVAL = 300;

    public ChromecastPlayerFragment() {
        // Required empty public constructor
    }


    // Overriden to get the ID from the currently playing media
    // on chromecast
    protected int getID() {
        if (mRemoteMediaClient != null) {
            MediaInfo mi = mRemoteMediaClient.getMediaInfo();
            if (mi != null && (mRemoteMediaClient.isPlaying() || mRemoteMediaClient.isPaused())) {
                MediaMetadata meta = mi.getMetadata();
                if (meta != null) {
                    int id = meta.getInt("ID");
                    if (id > 0)
                        return id;
                }
            }
        }
        return super.getID();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupMediaClient();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupMediaClient() {
        if (mRemoteMediaClient == null)
            mRemoteMediaClient = ChromeCastConfiguration.getRemoteMediaClient(this.getContext());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!this.isPlaying())
            startMedia();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMediaClient();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRemoteMediaClient != null)
            mRemoteMediaClient.addProgressListener(this, PROGRESS_LISTENER_INTERVAL);
    }

    @Override
    public void onStartMovieComplete(JukeboxDomain.JukeboxResponseStartMovie response) {
        if (response != null) {
            startCastVideo(
                    getCurrentTitle(),
                    response.getUri(),
                    response.getSubtitleUrisList(),
                    response.getSubtitleList(),
                    response.getMimeType());
        }
    }


    public void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri) {
        int id = getMediaTrackID(mRemoteMediaClient, subtitleUri);
        if (id >= 0)
            mRemoteMediaClient.setActiveMediaTracks(new long[]{(long) id});
    }

    @Override
    public void start() {
        mRemoteMediaClient.play();
    }

    @Override
    public void pause() {
        if (mRemoteMediaClient != null)
            mRemoteMediaClient.pause();
    }

    @Override
    public void stop() {
        if (mRemoteMediaClient != null) {
            mRemoteMediaClient.stop();
        }
    }

    @Override
    public int getDuration() {
        if (mRemoteMediaClient == null)
            return 0;
        else
            return (int) mRemoteMediaClient.getStreamDuration();
    }

    @Override
    public int getCurrentPosition() {
        if (mRemoteMediaClient == null)
            return 0;
        else
            return (int) mRemoteMediaClient.getApproximateStreamPosition() / 1000;
    }


    @Override
    public void seekTo(int position) {
        Activity a = getActivity();
        if (mRemoteMediaClient != null && a != null) {
            a.runOnUiThread(() -> mRemoteMediaClient.seek(position * 1000L));

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
        //final Context mAppContext = getContext().getApplicationContext();
        final RemoteMediaClient.ProgressListener listener = this;

        Runnable myRunnable = () -> {
            if (mRemoteMediaClient != null) {
                mRemoteMediaClient.addProgressListener(listener, PROGRESS_LISTENER_INTERVAL);

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

                MediaLoadOptions.Builder mlo = new MediaLoadOptions.Builder()
                        .setAutoplay(true)
                        .setActiveTrackIds(activeTrackIds);

                long position = getCachedPosition(this.getMedia());
                if (position > 0)
                    mlo.setPlayPosition(position * 1000);

                mRemoteMediaClient.load(mi, mlo.build())
                        .setResultCallback(mediaChannelResult -> {
                            Status status = mediaChannelResult.getStatus();

                            if (status.isSuccess()) {
                                Logger.Log().d(String.format("MEDIALOAD -- Media load success :: %s", status.getStatusMessage()));
                            } else {
                                Logger.Log().d(String.format("MEDIALOAD -- Media load FAILURE :: %s", status.getStatusMessage()));
                            }

                            // Signal handler so app can continue
                            signalHandlerLock();
                        });

            }


        };
        mainHandler.post(myRunnable);


        // Wait until runnable completes
        waitForHandlerSignal();

    }

    private void waitForHandlerSignal() {
        handlerLock.lock();
        try {
            startVideoComplete.await();
        } catch (InterruptedException ex) {
            Logger.Log().e("Interrupted ::", ex);
        } finally {
            handlerLock.unlock();
        }
    }

    private void signalHandlerLock() {
        handlerLock.lock();
        try {
            startVideoComplete.signal();
        } finally {
            handlerLock.unlock();
        }
    }


    @Override
    public void onProgressUpdated(long currentPosition, long duration) {
        updateSeeker((int) currentPosition / 1000, (int) duration / 1000);
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

        for (int i = 0; i < subtitleUris.size(); i++) {
            if (i < subs.size()) {
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
        md.putInt("ID", this.getID());
        addMetadataImage(movieUrl, md);

        return md;
    }

    private void addMetadataImage(String movieUrl, MediaMetadata md) {
        int id = getID();

        if (id > 0) {

            try {
                URL movieUri = new URL(movieUrl);
                Uri imageUri =
                        Uri.parse(String.format("%s://%s:%s/%sthumb%s"
                                , movieUri.getProtocol()
                                , movieUri.getHost()
                                , movieUri.getPort()
                                , this.getRequestType() == JukeboxDomain.RequestType.TypeMovie ? "" : "epi"
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
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getContentId().equals(subtitleUri.getUrl()))
                return i;
        }

        return -1;
    }

    @NonNull
    private long[] getActiveTracks(List<String> subtitleUris) {
        if (subtitleUris.size() > 0)
            return new long[]{1};
        else
            return new long[]{};
    }

    /****
     * Override this to avoid seeking twice.
     * Chromecast initializes startposition in the load
     */
    @Override
    protected void seekToStartPosition() {

    }

    @Override
    public JukeboxDomain.SubtitleRequestType getSubtitleRequestType() {
        return JukeboxDomain.SubtitleRequestType.WebVTT;
    }
}
