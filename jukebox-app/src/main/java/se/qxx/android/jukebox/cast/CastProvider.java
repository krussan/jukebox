package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;

import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.media.VideoControllerView;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

public abstract class CastProvider implements VideoControllerView.MediaPlayerControl {

    public int getCurrentMediaIndex() {
        return currentMediaIndex;
    }

    public void setCurrentMediaIndex(int currentMediaIndex) {
        this.currentMediaIndex = currentMediaIndex;
    }

    public List<JukeboxDomain.Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<JukeboxDomain.Media> mediaList) {
        this.mediaList = mediaList;
    }

    public enum CastProviderMode  {
        Movie,
        Episode
    }

    private Activity parentContext;
    private JukeboxConnectionProgressDialog dialog;
    private JukeboxConnectionHandler comm;
    private String title;
    private SeekerListener seekerListener;
    private int ID;
    private CastProviderMode mode;
    private JukeboxDomain.Movie movie;
    private JukeboxDomain.Episode episode;
    private SurfaceHolder display;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private List<JukeboxDomain.Media> mediaList = new ArrayList<>();
    private int currentMediaIndex = 0;


    public JukeboxDomain.Media getCurrentMedia() {
        return this.getMediaList().get(this.getCurrentMediaIndex());
    }

    public Activity getParentContext() {
        return this.parentContext;
    }

    public JukeboxConnectionProgressDialog getDialog() {
        return dialog;
    }

    public JukeboxConnectionHandler getJukeboxConnectionHandler() {
        return comm;
    }

    public String getTitle() {
        return title;
    }

    public SeekerListener getSeekerListener() {
        return seekerListener;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public int getID() {
        return this.ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    private void setCastProviderMode(CastProviderMode mode) {
        this.mode = mode;
    }

    public CastProviderMode getCastProviderMode() {
        return mode;
    }

    public JukeboxDomain.Movie getMovie() {
        return movie;
    }

    public void setMovie(JukeboxDomain.Movie movie) {
        this.movie = movie;
    }

    public JukeboxDomain.Episode getEpisode() {
        return episode;
    }

    public void setEpisode(JukeboxDomain.Episode episode) {
        this.episode = episode;
    }

    public SurfaceHolder getDisplay() {
        return display;
    }

    public void setDisplay(SurfaceHolder display) {
        this.display = display;
    }

    public MediaPlayer.OnPreparedListener getOnPreparedListener() {
        return this.onPreparedListener;
    }

    protected CastProvider() {

    }


    public static CastProvider getCaster(
            Activity parentContext,
            JukeboxConnectionHandler comm,
            JukeboxConnectionProgressDialog dialog,
            SeekerListener listener,
            SurfaceHolder display,
            MediaPlayer.OnPreparedListener onPreparedListener) {

        CastProvider provider = null;
        switch (ChromeCastConfiguration.getCastType()) {
            case ChromeCast:
                if (ChromeCastConfiguration.isChromecastConnected())
                    provider = new ChromeCastProvider();
                else
                    provider = new LocalCastProvider();
                break;
            case JukeboxCast:
                provider = new JukeboxCastProvider();
                break;
            default:
                provider = new LocalCastProvider();
        }

        provider.setup(parentContext, comm, dialog, listener, display, onPreparedListener);
        provider.initialize();

        return provider;
    }

    public static boolean isLocalPlayer() {
        switch (ChromeCastConfiguration.getCastType()) {
            case ChromeCast:
                if (ChromeCastConfiguration.isChromecastConnected())
                    return false;
                else
                    return true;
            case JukeboxCast:
                return false;
            default:
                return true;
        }
    }

    public void startMovie() {
        // override local player with chromecast in server (move this to server in time)
        // this to force a publish of the streams

        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (StringUtils.equalsIgnoreCase(player, "local"))
            player = "ChromeCast";

        comm.startMovie(
                player,
                this.getMovie(),
                this.getEpisode(),
                getCallback());
    }


    public void initialize(JukeboxDomain.Movie movie){
        this.setTitle(movie.getIdentifiedTitle());
        this.setID(movie.getID());
        this.setMovie(movie);
        this.setMediaList(movie.getMediaList());
    }

    public void initialize(JukeboxDomain.Episode episode) {
        this.setTitle(episode.getTitle());
        this.setID(episode.getID());
        this.setEpisode(episode);
        this.setMediaList(episode.getMediaList());
    }

    private void setup(
            Activity parentContext,
            JukeboxConnectionHandler comm,
            JukeboxConnectionProgressDialog dialog,
            SeekerListener listener,
            SurfaceHolder display,
            MediaPlayer.OnPreparedListener onPreparedListener) {

        this.setMediaList(mediaList);
        this.setCurrentMediaIndex(0);
        this.parentContext = parentContext;
        this.dialog = dialog;
        this.comm = comm;
        this.seekerListener = listener;
        this.display = display;
        this.onPreparedListener = onPreparedListener;
    }

    protected void closeDialog() {
        if (this.getDialog() != null)
            this.getDialog().close();
    }

    protected void initializeSubtitles() {

    }

    public abstract void initialize();
    public abstract RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback();
    public abstract void stop();
    public abstract boolean usesMediaController();
    public abstract void surfaceCreated(SurfaceView view);
    public abstract void surfaceDestroyed();
}
