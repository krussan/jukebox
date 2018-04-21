package se.qxx.android.jukebox.activities;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.comm.OnListSubtitlesCompleteHandler;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.widgets.Seeker;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.jukebox.widgets.UpdateSeekIndicator;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NowPlayingActivity
    extends AppCompatActivity
    implements OnSeekBarChangeListener, SeekerListener,
        RemoteMediaClient.ProgressListener, JukeboxResponseListener {

    private Seeker seeker;
    private boolean isManualSeeking = false;
    private JukeboxConnectionHandler comm;
    private CastContext mCastContext;
    private SessionManagerListener mSessionManagerListener;
    private CastSession mCastSession;

    private String getMode() {
        return getIntent().getExtras().getString("mode");
    }

    private boolean isMovieMode() {
        return StringUtils.equalsIgnoreCase(this.getMode(), "main") || StringUtils.isEmpty(this.getMode());
    }

    private boolean isEpisodeMode() { return StringUtils.equalsIgnoreCase(this.getMode(), "episode"); }

    //region --CALLBACKS--

    private class OnStatusComplete implements RpcCallback<JukeboxResponseIsPlaying> {
        @Override
        public void run(JukeboxResponseIsPlaying response) {
            Logger.Log().d("Response --- IsPlaying");
            if (response != null) {
                if (response.getIsPlaying()) {
                    Logger.Log().d("Request --- GetTitle");
                    comm.getTitle(JukeboxSettings.get().getCurrentMediaPlayer(), new OnGetTitleComplete());
                } else {
                    Logger.Log().d("Request --- StartMovie");
                    startMovie();
                }
            }
        }
    }

    private class OnGetTitleComplete implements RpcCallback<JukeboxResponseGetTitle> {
        @Override
        public void run(JukeboxResponseGetTitle response) {
            Logger.Log().d("Response --- GetTitle");
            if (response != null) {
                String playerFilename = response.getTitle();
                final Media md = matchCurrentFilenameAgainstMedia(playerFilename);
                if (md != null) {
                    //initialize seeker and get subtitles if app has been reinitialized
                    Model.get().setCurrentMedia(md);
                    initializeSeeker(Model.get().getCurrentMedia().getMetaDuration());

                    Thread t1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Logger.Log().d("Request --- ListSubtitles");
                            comm.listSubtitles(md, new OnListSubtitlesCompleteHandler());
                        }
                    });
                    t1.start();

                    //Start seeker and get time asap as the movie is playing
                    seeker.start(true);
                }
            } else {
                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.Log().d("Request --- StopMovie");
                        comm.stopMovie(JukeboxSettings.get().getCurrentMediaPlayer(), new OnStopMovieComplete());
                    }
                });
                t2.start();
            }
        }
    }

    private class OnStartMovieComplete implements RpcCallback<JukeboxResponseStartMovie> {
        @Override
        public void run(JukeboxResponseStartMovie response) {
            Logger.Log().d("Response --- StartMovie");
            Model.get().clearSubtitles();
            Model.get().addAllSubtitles(response.getSubtitleList());
            Model.get().setCurrentMedia(0);
            initializeSeeker(Model.get().getCurrentMedia().getMetaDuration());

            seeker.start();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.Log().d("Request --- ListSubtitles");
                    comm.listSubtitles(Model.get().getCurrentMedia(), new OnListSubtitlesCompleteHandler());
                }
            });
            t.start();
        }
    }

    private class OnChromecastStartComplete implements RpcCallback<JukeboxResponseStartMovie> {

        private Activity parentContext;

        private String title;

        public OnChromecastStartComplete(Activity parentContext, String title) {
            this.parentContext = parentContext;
            this.title = title;
        }

        /***
         * This is called when a movie was selected to play on a chromecast device
         * and the file has successfully registered with the http server
         * Sets up the chromecast stream.
         *
         * @param response
         */
        @Override
        public void run(JukeboxResponseStartMovie response) {

            initializeSeeker(Model.get().getCurrentMedia().getMetaDuration());

            if (response != null) {
                startCastVideo(this.title, response.getUri(), response.getSubtitleUrisList(), response.getSubtitleList());

                // update the subtitles out of sync
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.Log().d("Request --- ListSubtitles");
                        comm.listSubtitles(Model.get().getCurrentMedia(), new OnListSubtitlesCompleteHandler());
                    }
                });
                t.start();
            }
        }
    }

    private String getHeadTitle() {
        if (this.isMovieMode())
            return Model.get().getCurrentMovie().getTitle();

        if (this.isEpisodeMode())
            return Model.get().getCurrentEpisode().getTitle();

        return StringUtils.EMPTY;
    }

    private class OnStopMovieComplete implements RpcCallback<Empty> {
        @Override
        public void run(Empty arg0) {
            Logger.Log().d("Response --- StopMovie");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.Log().d("Request --- StartMovie");
                    startMovie();
                }
            });
            t.start();
        }
    }

    //endregion

    //region --Initialization--

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        comm = new JukeboxConnectionHandler(JukeboxSettings.get().getServerIpAddress(), JukeboxSettings.get().getServerPort());
        comm.setListener(this);

        setContentView(R.layout.nowplaying);

        mCastContext = CastContext.getSharedInstance(this);

        initializeView();
    }

    private void initializeView() {
        try {
            if (this.isEpisodeMode()) {
                Episode ep = Model.get().getCurrentEpisode();
                initializeView(String.format("S%sE%s - %s",
                        Model.get().getCurrentSeason().getSeasonNumber(),
                        ep.getEpisodeNumber(),
                        ep.getTitle()),
                    ep.getImage());

            }
            else {
                Movie m = Model.get().getCurrentMovie();
                initializeView(m.getTitle(), m.getImage());
            }

            SeekBar sb = (SeekBar) findViewById(R.id.seekBarDuration);
            sb.setOnSeekBarChangeListener(this);

            if (ChromeCastConfiguration.isChromeCastActive())
                initializeChromecast();
            else
                initializeJukeboxCast();

        } catch (Exception e) {
            Logger.Log().e("Unable to initialize NowPlayingActivity", e);
        }
    }

    private void initializeView(String title, ByteString image) {
        View rootView = GUITools.getRootView(this);

        if (!image.isEmpty()) {
            Bitmap bm = GUITools.getBitmapFromByteArray(image.toByteArray());
            Bitmap scaledImage = GUITools.scaleImage(300, bm, this);
            GUITools.setImageOnImageView(R.id.imgNowPlaying, scaledImage, rootView);
        }

        GUITools.setTextOnTextview(R.id.lblNowPlayingTitle, title, rootView);

    }

    private void initializeJukeboxCast() {
        seeker = new Seeker(this);

        Logger.Log().d("Request -- IsPlaying");
        comm.isPlaying(JukeboxSettings.get().getCurrentMediaPlayer(), new OnStatusComplete());
    }

    private void initializeChromecast() {
        setupCastListener();
        startMovie();

    }

    //endregion

    //region --Activity overrides--

    @Override
    protected void onPause() {
        super.onPause();
        if (seeker != null)
            seeker.stop();
    }

    ;

    @Override
    protected void onStop() {
        super.onStop();
        if (seeker != null)
            seeker.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (seeker != null)
            seeker.start();
    }

    ;

    //endregion

    //region --SEEKBAR--

    @Override
    public void onProgressUpdated(long currentPosition, long duration) {
        // position and duration from cast libraries are in milliseconds
        if (!this.isManualSeeking) {
            SeekBar sb = (SeekBar) findViewById(R.id.seekBarDuration);
            if (sb != null) {
                //sb.setMax(duration / 1000);
                sb.setProgress((int)currentPosition / 1000);
            }

            updateSeekbarText((int)currentPosition / 1000);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        if (this.isManualSeeking)
            updateSeekbarText(progress);
    }

    private void updateSeekbarText(int progress) {
        final TextView tv = (TextView) findViewById(R.id.txtSeekIndicator);

        runOnUiThread(new UpdateSeekIndicator(progress, tv));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.isManualSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final TextView tv = (TextView) findViewById(R.id.txtSeekIndicator);

        int seconds = seekBar.getProgress();

        Logger.Log().d("Request --- Seek");
        if (ChromeCastConfiguration.isChromeCastActive()) {
            RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this.getApplicationContext());

            if (client != null) {
                client.seek(seconds * 1000);
            }
        }
        else {
            comm.seek(JukeboxSettings.get().getCurrentMediaPlayer(), seconds);
        }

        this.isManualSeeking = false;
    }

    @Override
    public void updateSeeker(final int seconds) {
        final TextView tv = (TextView) findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBarDuration);

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds, tv, seekBar));
    }

    @Override
    public void increaseSeeker(int advanceSeconds) {
        final TextView tv = (TextView) findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBarDuration);
        int seconds = seekBar.getProgress();

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds + advanceSeconds, tv, seekBar));
    }

    private void initializeSeeker(int duration) {
        SeekBar sb = (SeekBar) findViewById(R.id.seekBarDuration);
        if (sb != null && sb.getMax() != duration)
            sb.setMax(duration);
    }


    //endregion

    //region --BUTTONS--

    public void onButtonClicked(View v) {
        int id = v.getId();
        GUITools.vibrate(28, this);

        switch (id) {
            case R.id.btnPlay:
                Logger.Log().d("Request --- StartMovie");
                startMovie();
                break;
            case R.id.btnFullscreen:
                Logger.Log().d("Request --- ToggleFullScreen");
                setupFullscreen();
                break;
            case R.id.btnPause:
                Logger.Log().d("Request --- Pause");
                pauseMovie();
                break;
            case R.id.btnStop:
                Logger.Log().d("Request --- StopMove");
                stopMovie();
                this.finish();
                break;
            case R.id.btnViewInfo:
                String url = Model.get().getCurrentMovie().getImdbUrl();
                if (url != null && url.length() > 0) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSubSelection:
                Intent i = new Intent(this, SubSelectActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }

    private void stopMovie() {
        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (ChromeCastConfiguration.isChromeCastActive()) {
            RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this.getApplicationContext());

            if (client != null) {
                client.removeProgressListener(this);
                client.stop();
            }
        }
        else {
            seeker.stop();
            comm.stopMovie(player, null);
        }
    }

    private void pauseMovie() {
        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (ChromeCastConfiguration.isChromeCastActive()) {
            RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this.getApplicationContext());

            if (client != null) {
                client.togglePlayback();
            }
        }
        else {
            seeker.toggle();
            comm.pauseMovie(player);
        }
    }

    private void setupFullscreen() {

        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (!ChromeCastConfiguration.isChromeCastActive()) {
            comm.toggleFullscreen(player);
        }

    }


    private void startMovie() {
        RpcCallback<JukeboxResponseStartMovie> callback;

        if (ChromeCastConfiguration.isChromeCastActive()) {
            callback = new OnChromecastStartComplete(this, this.getHeadTitle());
        } else {
            callback = new OnStartMovieComplete();
        }

        Movie m = null;
        Episode ep = null;
        if (this.isMovieMode())
            m = Model.get().getCurrentMovie();

        if (this.isEpisodeMode())
            ep = Model.get().getCurrentEpisode();

        comm.startMovie(
                JukeboxSettings.get().getCurrentMediaPlayer(),
                m,
                ep,
                callback);

    }

    //endregion

    //region --HELPERS--

    protected Media matchCurrentFilenameAgainstMedia(String playerFilename) {
        for (Media md : Model.get().getCurrentMovie().getMediaList()) {
            if (StringUtils.equalsIgnoreCase(playerFilename, md.getFilename())) {
                return md;
            }
        }

        return null;
    }

    //endregion

    //region --MENU--

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

        return true;
    }


    //endregion

    public void startCastVideo(final String title, final String movieUrl, final List<String> subtitleUris, final List<JukeboxDomain.Subtitle> subs) {
        // Since this could be called from a callback we need to trigger it
        // on the main thread.

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(this.getMainLooper());
        final Context mAppContext = this.getApplicationContext();
        final RemoteMediaClient.ProgressListener listener = this;
        final Activity parentActivity = this;

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


                    MediaLoadOptions mlo
                    client.load(mi, false, 0, activeTrackIds, null)
                            .setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {

                                @Override
                                public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
                                    Status status = mediaChannelResult.getStatus();

                                    if (status.isSuccess()) {
                                        // on load success play the movie?
                                        if (!client.isPlaying())
                                            client.play();

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

//                    String baseUrl = StringUtils.EMPTY;
//                    try {
//                        URL movieUri = new URL(movieUrl);
//                        String base = movieUri.getProtocol() + "://" + movieUri.getHost() + "/thumb";
//                    } catch (MalformedURLException e) {
//                    }

//                    String base = movieUri.getProtocol() + "://" + url.getHost() + path;
//                    md.addImage(new WebImage(new Uri(movieUri).get));


                return md;
            }

            private long[] getActiveTracks() {
                long[] activeTrackIds = null;
                if (subtitleUris.size() > 0)
                    activeTrackIds = new long[] {1};
                return activeTrackIds;
            }

            private List<MediaTrack> getSubtitleTracks() {
                List<MediaTrack> tracks = new ArrayList<MediaTrack>();

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

    /***
     * Handles request complete from JukeboxResponseListener
     * @param message
     */
    public void onRequestComplete(JukeboxConnectionMessage message) {
        if (!message.result()) {
            Toast.makeText(this, "Failed :: " + message.getMessage(),Toast.LENGTH_LONG);
        }
    }



    private void setupCastListener() {
        RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this);
        final Context context = this.getApplicationContext();

        client.addListener(new RemoteMediaClient.Listener() {
            /***
             * Handles status updates from RemoteMediaClient.Listener
             */
            @Override
            public void onPreloadStatusUpdated() {
                RemoteMediaClient remoteMediaClient = ChromeCastConfiguration.getRemoteMediaClient(context);

                if (remoteMediaClient == null) {
                    return;
                }
                MediaStatus mediaStatus = remoteMediaClient.getMediaStatus();
                if (mediaStatus == null) {
                    return;
                }

            }

            /***
             * Handles status updates from RemoteMediaClient.Listener
             */
            @Override
            public void onQueueStatusUpdated() {

            }

            /***
             * Handles status updates from RemoteMediaClient.Listener
             */
            @Override
            public void onStatusUpdated() {
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


            /***
             * Handles status updates from RemoteMediaClient.Listener
             */
            @Override
            public void onMetadataUpdated() {
                RemoteMediaClient remoteMediaClient = ChromeCastConfiguration.getRemoteMediaClient(context);
                if (remoteMediaClient == null) {
                    return;
                }
                MediaStatus mediaStatus = remoteMediaClient.getMediaStatus();
                if (mediaStatus == null) {
                    return;
                }
            }

            /***
             * Handles status updates from RemoteMediaClient.Listener
             */
            @Override
            public void onSendingRemoteMediaRequest() {
            }
        });
    }
}
