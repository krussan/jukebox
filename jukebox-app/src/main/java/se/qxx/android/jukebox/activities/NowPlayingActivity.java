package se.qxx.android.jukebox.activities;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.cast.CastProvider;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.cast.JukeboxCastType;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.CastSession;
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
    implements OnSeekBarChangeListener, SeekerListener, JukeboxResponseListener {

    private Seeker seeker;
    private boolean isManualSeeking = false;
    private JukeboxConnectionHandler comm;

    private SessionManagerListener mSessionManagerListener;
    private CastSession mCastSession;
    private CastProvider castProvider;

    private String getMode() {
        Intent i = getIntent();
        if (i != null) {
            Bundle b = i.getExtras();
            if (b != null) {
                return b.getString("mode");
            }
        }
        return StringUtils.EMPTY;
    }


    private boolean isEpisodeMode() { return StringUtils.equalsIgnoreCase(this.getMode(), "episode"); }

    //region --Initialization--

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        comm = new JukeboxConnectionHandler(JukeboxSettings.get().getServerIpAddress(), JukeboxSettings.get().getServerPort());
        comm.setListener(this);

        setContentView(R.layout.nowplaying);

        initializeView();
    }

    private void initializeView() {
        try {
            SeekBar sb = findViewById(R.id.seekBarDuration);
            sb.setOnSeekBarChangeListener(this);



            //JukeboxConnectionProgressDialog dialog =
//                    JukeboxConnectionProgressDialog.build(this, "Starting media ...");


            SurfaceHolder holder = getSurfaceHolder();
            castProvider = CastProvider.getCaster(this, this.comm, null, this, holder);

            if (this.isEpisodeMode()) {
                Episode ep = Model.get().getCurrentEpisode();
                initializeView(
                        String.format("S%sE%s - %s",
                        Model.get().getCurrentSeason().getSeasonNumber(),
                        ep.getEpisodeNumber(),
                        ep.getTitle()),
                        ep.getImage());

                castProvider.initialize(ep);

            }
            else {
                Movie m = Model.get().getCurrentMovie();
                initializeView(m.getTitle(), m.getImage());
                castProvider.initialize(m);
            }

            castProvider.startMovie();


        } catch (Exception e) {
            Logger.Log().e("Unable to initialize NowPlayingActivity", e);
        }
    }

    @NonNull
    private SurfaceHolder getSurfaceHolder() {
        SurfaceView view = findViewById(R.id.surfaceview);
        SurfaceHolder holder = view.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

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
            updateSeekbarText(progress);
    }

    private void updateSeekbarText(int progress) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);

        runOnUiThread(new UpdateSeekIndicator(progress, tv));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.isManualSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);

        int seconds = seekBar.getProgress();

        Logger.Log().d("Request --- Seek");
        castProvider.seek(seconds);

        this.isManualSeeking = false;
    }

    @Override
    public void updateSeeker(final int seconds) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = findViewById(R.id.seekBarDuration);

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds, tv, seekBar));

    }

    @Override
    public void increaseSeeker(int advanceSeconds) {
        final TextView tv = findViewById(R.id.txtSeekIndicator);
        final SeekBar seekBar = findViewById(R.id.seekBarDuration);
        int seconds = seekBar.getProgress();

        if (!this.isManualSeeking)
            runOnUiThread(new UpdateSeekIndicator(seconds + advanceSeconds, tv, seekBar));
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
    public void onRequestComplete(JukeboxConnectionMessage message) {
        if (!message.result()) {
            Toast.makeText(this, "Failed :: " + message.getMessage(),Toast.LENGTH_LONG).show();
        }
    }



}
