package se.qxx.android.jukebox.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.protobuf.ByteString;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.cast.CastProvider;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.widgets.Seeker;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.jukebox.widgets.UpdateSeekIndicator;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class NowPlayingActivity
    extends AppCompatActivity
        implements OnSeekBarChangeListener, SeekerListener, JukeboxResponseListener, MediaPlayer.OnPreparedListener, SessionManagerListener<Session> {

    private Seeker seeker;
    private boolean isManualSeeking = false;
    private JukeboxConnectionHandler comm;

    private CastProvider castProvider;
    MediaController mcontroller ;
    private List<JukeboxDomain.Media> mediaList;
    private int currentMediaIndex = 0;
    private String imdbUrl = StringUtils.EMPTY;

    private boolean loadingVisible = false;
    private static boolean screenChange = false;

    private ViewMode getMode() {
        Intent i = getIntent();
        if (i != null) {
            Bundle b = i.getExtras();
            if (b != null) {
                return (ViewMode)b.getSerializable("mode");
            }
        }
        return ViewMode.Movie;
    }

    private JukeboxDomain.RequestType getRequestType() {
        if (this.getMode() == ViewMode.Episode)
            return JukeboxDomain.RequestType.TypeEpisode;
        else
            return JukeboxDomain.RequestType.TypeMovie;
    }


    //region --Initialization--

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nowplaying);

        comm = new JukeboxConnectionHandler(
                JukeboxSettings.get().getServerIpAddress(),
                JukeboxSettings.get().getServerPort());

        comm.setListener(this);

        initializeCastProvider();
        initializeView();

        screenChange = false;
    }

    private boolean isLocalPlayer() {
        if (castProvider == null)
            return true;

        return castProvider.usesMediaController();
    }

    private void initializeView() {
        try {

            SeekBar sb = findViewById(R.id.seekBarDuration);
            sb.setOnSeekBarChangeListener(this);
            sb.setVisibility(View.VISIBLE);

            comm.getItem(this.getID(), this.getRequestType(), false, true,
                response -> {
                    initializeView(this.getRequestType(), response);
                });


        } catch (Exception e) {
            Logger.Log().e("Unable to initialize NowPlayingActivity", e);
        }
    }

    private void initializeView(JukeboxDomain.RequestType requestType, JukeboxDomain.JukeboxResponseGetItem response) {

        if (requestType == JukeboxDomain.RequestType.TypeEpisode) {
            Episode ep = response.getEpisode();

            if (ep != null) {
                this.setMediaList(ep.getMediaList());
                this.setCurrentMediaIndex(0);
                this.setImdbUrl(ep.getImdbUrl());

                initializeView(
                        String.format("S%sE%s - %s",
                                this.getSeasonNumber(),
                                ep.getEpisodeNumber(),
                                ep.getTitle()),
                        ep.getImage());

                castProvider.initialize(ep);
            }
        }
        else if (requestType == JukeboxDomain.RequestType.TypeMovie) {
            Movie m = response.getMovie();

            this.setImdbUrl(m.getImdbUrl());
            if (m!= null) {
                this.setMediaList(m.getMediaList());
                this.setCurrentMediaIndex(0);
                initializeView(m.getTitle(), m.getImage());
                castProvider.initialize(m);
            }
        }

        initializeMediaController();
        initializeSessionManager();

        if (!screenChange)
            startMedia();

    }

    private void startMedia() {
        loadingVisible = true;
        setVisibility(isLocalPlayer());

        castProvider.startMovie();
    }

    private void initializeCastProvider() {
        SurfaceHolder holder = getSurfaceHolder();

        castProvider = CastProvider.getCaster(
                this,
                this.comm,
                null,
                this,
                holder,
                this);
    }

    private void initializeSessionManager() {
        SessionManager sessionManager = CastContext.getSharedInstance().getSessionManager();

        if (sessionManager != null) {
            sessionManager.addSessionManagerListener(this);
        }
    }

    private void initializeMediaController() {

        SurfaceView sv = findViewById(R.id.surfaceview);
        boolean surfaceViewVisible = isLocalPlayer();
        setVisibility(surfaceViewVisible);

        if (surfaceViewVisible) {
            mcontroller = new MediaController(this);
            mcontroller.setMediaPlayer(castProvider);
        }
    }

    private void setVisibility(boolean surfaceViewVisible) {
        SurfaceView sv = findViewById(R.id.surfaceview);

        SeekBar sb = findViewById(R.id.seekBarDuration);
        LinearLayout linearLayout2 = findViewById(R.id.linearLayout2);
        LinearLayout linearLayout1 = findViewById(R.id.linearLayout1);
        LinearLayout linearLayoutButtons1 = findViewById(R.id.linearLayoutButtons1);
        TextView txtSeekIndicator = findViewById(R.id.txtSeekIndicator);
        ProgressBar spinner = findViewById(R.id.spinner);

        int standardControlsMode = surfaceViewVisible || loadingVisible ? View.GONE : View.VISIBLE;
        int mediaControllerMode = !surfaceViewVisible || loadingVisible ? View.GONE : View.VISIBLE;
        int spinnerVisible = loadingVisible ? View.VISIBLE : View.GONE;

        sb.setVisibility(standardControlsMode);
        linearLayout1.setVisibility(standardControlsMode);
        linearLayout2.setVisibility(standardControlsMode);
        linearLayoutButtons1.setVisibility(standardControlsMode);
        txtSeekIndicator.setVisibility(standardControlsMode);

        sv.setVisibility(mediaControllerMode);

        spinner.setVisibility(spinnerVisible);


    }

    @NonNull
    private SurfaceHolder getSurfaceHolder() {
        final SurfaceView view = findViewById(R.id.surfaceview);
        SurfaceHolder holder = view.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                castProvider.surfaceCreated(view);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
        return holder;
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

    //endregion

    //region --Activity overrides--

    @Override
    protected void onPause() {
        super.onPause();
        if (seeker != null)
            seeker.stop();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (seeker != null)
            seeker.stop();

        if (castProvider != null && isLocalPlayer())
            castProvider.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (seeker != null)
            seeker.start();
    }

    //endregion

    //region --SEEKBAR--

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        if (this.isManualSeeking)
            updateSeekbarText(progress, seekBar.getMax());
    }

    private void updateSeekbarText(long progress, long duration) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);

        runOnUiThread(new UpdateSeekIndicator(progress, duration, tv));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.isManualSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int seconds = seekBar.getProgress();

        Logger.Log().d("Request --- Seek");
        castProvider.seekTo(seconds);

        this.isManualSeeking = false;
    }

    @Override
    public void updateSeeker(final long seconds, final long duration) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = findViewById(R.id.seekBarDuration);

        final long actualDuration = duration == 0 ? seekBar.getMax() : duration;

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds, actualDuration, tv, seekBar));

    }

    @Override
    public void increaseSeeker(int advanceSeconds) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = findViewById(R.id.seekBarDuration);
        int seconds = seekBar.getProgress();

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds + advanceSeconds, seekBar.getMax(), tv, seekBar));
    }

    @Override
    public void setDuration(int seconds) {
        SeekBar sb = findViewById(R.id.seekBarDuration);
        if (sb != null && sb.getMax() != seconds)
            sb.setMax(seconds);
    }

    @Override
    public void initializeSeeker() {
        seeker = new Seeker(this);
    }

    @Override
    public void startSeekerTimer() {
        seeker.start();
    }

    @Override
    public void stopSeekerTimer() {
        seeker.stop();
    }


    //endregion

    //region --BUTTONS--

    public void onButtonClicked(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnPlay:
                Logger.Log().d("Request --- StartMovie");
                castProvider.startMovie();
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
                castProvider.stop();
                this.finish();
                break;
            case R.id.btnViewInfo:
                if (StringUtils.isNotEmpty(this.getImdbUrl())) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getImdbUrl()));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnSubSelection:
                Intent i = new Intent(this, SubSelectActivity.class);
                i.putExtra("media", this.getMediaList().get(this.getCurrentMediaIndex()));
                startActivity(i);
                break;
            default:
                break;
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


    //endregion

    //region --MENU--

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

        return true;
    }


    //endregion

    public void startLocalVideo(final int movieId, final String title, final String movieUrl, final List<String> subtitleUris, final List<JukeboxDomain.Subtitle> subs) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(movieUrl));
        mediaPlayer.start();
    }


    /***
     * Handles request complete from JukeboxResponseListener
     * @param message
     */
    public void onRequestComplete(final JukeboxConnectionMessage message) {
        if (!message.result()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NowPlayingActivity.this,
                            "Failed :: " + message.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }


        loadingVisible = false;
        runOnUiThread(() -> {
            setVisibility(isLocalPlayer());
        });

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isLocalPlayer()) {
            mcontroller.setMediaPlayer(castProvider);

            mcontroller.setAnchorView(findViewById(R.id.surfaceview));
            mcontroller.setEnabled(true);

            new Handler().post(new Runnable() {
                public void run() {
                    mcontroller.show();
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isLocalPlayer())
            mcontroller.show();

        return false;
    }


    @Override
    protected void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);

        screenChange = true;
    }

    @Override
    public void onSessionStarting(Session session) {
        // stop the current cast provider
        if (castProvider != null) {
            castProvider.stop();
        }
    }

    @Override
    public void onSessionStarted(Session session, String s) {
        initializeCastProvider();
        // start movie and seek?
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {

    }

    @Override
    public void onSessionEnding(Session session) {
    }

    @Override
    public void onSessionEnded(Session session, int i) {
        // stop the current cast provider
        if (castProvider != null) {
            if (castProvider.isPlaying()) {
                castProvider.stop();
                initializeCastProvider();
            }
        }

        // start movie and seek?
    }

    @Override
    public void onSessionResuming(Session session, String s) {

    }

    @Override
    public void onSessionResumed(Session session, boolean b) {

    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {

    }

    @Override
    public void onSessionSuspended(Session session, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CastContext.getSharedInstance().getSessionManager().removeSessionManagerListener(this);
    }

    private int getSeasonNumber() {
        Bundle b = getIntent().getExtras();
        if (b != null)
            return b.getInt("seasonNumber");

        return 0;
    }

    private int getID() {
        Bundle b = getIntent().getExtras();
        if (b != null)
            return b.getInt("ID");

        return -1;
    }

    public List<JukeboxDomain.Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<JukeboxDomain.Media> mediaList) {
        this.mediaList = mediaList;
    }

    public int getCurrentMediaIndex() {
        return currentMediaIndex;
    }

    public void setCurrentMediaIndex(int currentMediaIndex) {
        this.currentMediaIndex = currentMediaIndex;
    }

    public String getImdbUrl() {
        return imdbUrl;
    }

    public void setImdbUrl(String imdbUrl) {
        this.imdbUrl = imdbUrl;
    }
}
