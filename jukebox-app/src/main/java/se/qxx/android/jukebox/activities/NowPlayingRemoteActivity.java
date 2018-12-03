package se.qxx.android.jukebox.activities;

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
import se.qxx.android.jukebox.media.VideoControllerView;
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

public class NowPlayingRemoteActivity
    extends AppCompatActivity
        implements OnSeekBarChangeListener, SeekerListener, JukeboxResponseListener, SessionManagerListener<Session> {

    private Seeker seeker;
    private boolean isManualSeeking = false;
    private JukeboxConnectionHandler comm;
    private VideoControllerView controller;

    private CastProvider castProvider;

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



    //region --Initialization--

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nowplaying_local);

        initializeView();

        screenChange = false;
    }

    private boolean isLocalPlayer() {
        return castProvider == null || castProvider.usesMediaController();
    }

    private void initializeView() {
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


    /***
     * Handles request complete from JukeboxResponseListener
     * @param message
     */
    public void onRequestComplete(final JukeboxConnectionMessage message) {
        if (!message.result()) {
            runOnUiThread(() -> Toast.makeText(NowPlayingRemoteActivity.this,
                    "Failed :: " + message.getMessage(),
                    Toast.LENGTH_LONG).show());
        }


        loadingVisible = false;
        runOnUiThread(() -> setVisibility(isLocalPlayer()));

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
