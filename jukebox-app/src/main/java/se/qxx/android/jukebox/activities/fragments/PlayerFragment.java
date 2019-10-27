package se.qxx.android.jukebox.activities.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.activities.fragments.SubtitleSelectFragment.SubtitleSelectDialogListener;
import se.qxx.android.jukebox.cast.JukeboxCastType;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain;

public abstract class PlayerFragment extends Fragment implements JukeboxResponseListener, SubtitleSelectDialogListener {

    private boolean loadingVisible;
    private JukeboxConnectionHandler connectionHandler;

    private List<JukeboxDomain.Media> mediaList;
    private int currentMediaIndex = 0;

    private String imdbUrl = StringUtils.EMPTY;
    private CacheData cacheData;

    private String currentTitle;
    private JukeboxDomain.Movie currentMovie;
    private JukeboxDomain.Episode currentEpisode;
    private JukeboxSettings settings;
    private int exitPosition;

    protected JukeboxSettings getSettings() {
        return settings;
    }

    Lock handlerLock = new ReentrantLock();
    Condition getVideoInfoComplete = handlerLock.newCondition();

    private int getSeasonNumber() {
        Bundle b = getActivity().getIntent().getExtras();
        if (b != null)
            return b.getInt("seasonNumber");

        return 0;
    }

    protected boolean getScreenChanged() {
        Bundle b = getArguments();
        if (b != null)
            return b.getBoolean("screenChanged");

        return false;
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

    public JukeboxConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(JukeboxConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public boolean getLoadingVisible() {
        return loadingVisible;
    }

    public void setLoadingVisible(boolean loadingVisible) {
        this.loadingVisible = loadingVisible;
    }

    protected JukeboxDomain.Media getMedia() {
        if (this.getMediaList() != null)
            return this.getMediaList().get(this.getCurrentMediaIndex());

        return null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheData = new CacheData(getContext());
        settings = new JukeboxSettings(getContext());

        this.setConnectionHandler(
            new JukeboxConnectionHandler(
                settings.getServerIpAddress(),
                settings.getServerPort()));

        this.getConnectionHandler()
            .setListener(this);

    }

    protected void initializeView(View v, final ByteString image) {
        getActivity().runOnUiThread(() -> {
            if (!image.isEmpty()) {
                Bitmap bm = GUITools.getBitmapFromByteArray(image.toByteArray());
                Bitmap scaledImage = GUITools.scaleImage(300, bm, getContext());
                GUITools.setImageOnImageView(R.id.imgNowPlaying, scaledImage, v);
            }

            GUITools.setTextOnTextview(R.id.lblNowPlayingTitle, getCurrentTitle(), v);
        });

    }

    @Override
    public void onRequestComplete(JukeboxConnectionMessage message) {
        if (!message.result()) {
            getActivity().runOnUiThread(() -> Toast.makeText(this.getContext(),
                    "Failed :: " + message.getMessage(),
                    Toast.LENGTH_LONG).show());
        }


        this.setLoadingVisible(false);
        getActivity().runOnUiThread(() -> setVisibility(getView()));
    }


    private ViewMode getMode() {
        Intent i = getActivity().getIntent();

        if (i != null) {
            Bundle b = i.getExtras();
            if (b != null) {
                return (ViewMode)b.getSerializable("mode");
            }
        }
        return ViewMode.Movie;
    }


    protected JukeboxDomain.RequestType getRequestType() {
        if (this.getMode() == ViewMode.Episode)
            return JukeboxDomain.RequestType.TypeEpisode;
        else
            return JukeboxDomain.RequestType.TypeMovie;

    }

    protected int getID() {
        Bundle b = getActivity().getIntent().getExtras();
        if (b != null)
            return b.getInt("ID");

        return -1;
    }

    protected void initializeView(View v, JukeboxDomain.RequestType requestType, JukeboxDomain.JukeboxResponseGetItem response) {

        if (requestType == JukeboxDomain.RequestType.TypeEpisode) {
            JukeboxDomain.Episode ep = response.getEpisode();

            if (ep != null) {
                this.setMediaList(ep.getMediaList());
                this.setCurrentMediaIndex(0);
                this.setImdbUrl(ep.getImdbUrl());

                initializeMedia(ep);
                initializeView(v, ep.getImage());

                //castProvider.initialize(ep);
            }
        }
        else if (requestType == JukeboxDomain.RequestType.TypeMovie) {
            JukeboxDomain.Movie m = response.getMovie();

            this.setImdbUrl(m.getImdbUrl());
            this.setMediaList(m.getMediaList());
            this.setCurrentMediaIndex(0);

            initializeMedia(m);
            initializeView(v, m.getImage());

            //castProvider.initialize(m);
        }

    }

    private void initializeMedia(JukeboxDomain.Movie m) {
        currentMovie = m;
        currentTitle = m.getIdentifiedTitle();
        currentEpisode = null;
    }

    private void initializeMedia(JukeboxDomain.Episode ep) {
        currentMovie = null;
        currentTitle = String.format("S%sE%s - %s",
                this.getSeasonNumber(),
                ep.getEpisodeNumber(),
                ep.getTitle());
        currentEpisode = ep;
    }

    protected void startMedia() {
        this.setLoadingVisible(true);
        getActivity().runOnUiThread(() -> setVisibility(getView()));

        // Wait for other thread to complete information retrieval
        waitForGetInfoComplete();

        this.getConnectionHandler()
            .startMovie(
                getPlayerName(),
                currentMovie,
                currentEpisode,
                getSubtitleRequestType(),
                    response -> {
                        onStartMovieComplete(response);
                        seekToStartPosition();
                    });
    }

    protected String getPlayerName() {
        String player = getSettings().getCurrentMediaPlayer();
        if (StringUtils.equalsIgnoreCase(player, "local"))
            player = "ChromeCast";

        return player;
    }

    public static Fragment newInstance(JukeboxCastType type, boolean screenChanged) {
        Bundle b = new Bundle();
        Fragment fm;

        if (type == JukeboxCastType.Local)
            fm = new LocalPlayerFragment();
        else if (type == JukeboxCastType.ChromeCast) {
            fm = new ChromecastPlayerFragment();
        } else
            fm = new JukeboxPlayerFragment();

        b.putBoolean("screenChanged", screenChanged);

        fm.setArguments(b);

        return fm;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getConnectionHandler().getItem(
            getID(),
            this.getRequestType(),
            false,
            true,
            response -> {
                if (response != null) {
                    initializeView(getView(), this.getRequestType(), response);

                    signalGetInfoCopmlete();
                    onGetItemCompleted();

                    getActivity().runOnUiThread(() -> {
                        setVisibility(getView());
                    });
                }
            });
    }

    @Override
    public void onPause() {
        super.onPause();

        //save media state
        if (this.getExitPosition() > 0)
            cacheData.saveMediaState(this.getMedia().getID(), getExitPosition());

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public abstract void setVisibility(View v);
    public abstract void onGetItemCompleted();
    //public abstract RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback();
    public abstract void onStartMovieComplete(JukeboxDomain.JukeboxResponseStartMovie response);
    public abstract void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri);
    public abstract void seekTo(int position);
    public abstract JukeboxDomain.SubtitleRequestType getSubtitleRequestType();

    protected void showSubtitleDialog() {
        FragmentManager fm = getFragmentManager();

        SubtitleSelectFragment subtitleSelectFragment = SubtitleSelectFragment.newInstance(this.getMedia(), this);
        subtitleSelectFragment.show(fm, "subtitleSelectFragment");

    }


    public String getCurrentTitle() {
        return this.currentTitle;
    }


    public int getExitPosition() {
        return exitPosition;
    }

    public void setExitPosition(int exitPosition) {
        this.exitPosition = exitPosition;
    }

    public int getCachedPosition(int mediaID) {
        return cacheData.getMediaState(mediaID);
    }

    /***
     * Gets the cached position for the media
     * and seeks to that position at start of media playback
     */
    protected void seekToStartPosition() {
        if (this.getMedia() != null) {
            // get media state
            int position = getCachedPosition(this.getMedia().getID());
            if (position > 0)
                seekTo(position * 1000);
        }
    }

    private void waitForGetInfoComplete() {
        handlerLock.lock();
        try {
            getVideoInfoComplete.await();
        } catch (InterruptedException ex) {
            Logger.Log().e("Interrupted ::", ex);
        } finally {
            handlerLock.unlock();
        }
    }

    private void signalGetInfoCopmlete() {
        handlerLock.lock();
        try {
            getVideoInfoComplete.signal();
        } finally {
            handlerLock.unlock();
        }
    }

}
