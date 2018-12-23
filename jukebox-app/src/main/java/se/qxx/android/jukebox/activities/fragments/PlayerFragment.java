package se.qxx.android.jukebox.activities.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.activities.fragments.SubtitleSelectFragment.SubtitleSelectDialogListener;
import se.qxx.android.jukebox.cast.CastProvider;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.cast.JukeboxCastType;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.tools.GUITools;
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
    private int currentId;
    private JukeboxDomain.Movie currentMovie;
    private JukeboxDomain.Episode currentEpisode;

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
        return this.getMediaList().get(this.getCurrentMediaIndex());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheData = new CacheData(getContext());
        this.setConnectionHandler(
            new JukeboxConnectionHandler(
                JukeboxSettings.get().getServerIpAddress(),
                JukeboxSettings.get().getServerPort()));

        this.getConnectionHandler()
            .setListener(this);

    }

    protected void initializeView(View v, final String title, final ByteString image) {
        getActivity().runOnUiThread(() -> {
            if (!image.isEmpty()) {
                Bitmap bm = GUITools.getBitmapFromByteArray(image.toByteArray());
                Bitmap scaledImage = GUITools.scaleImage(300, bm, getContext());
                GUITools.setImageOnImageView(R.id.imgNowPlaying, scaledImage, v);
            }

            GUITools.setTextOnTextview(R.id.lblNowPlayingTitle, title, v);
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

                initializeView(v,
                        String.format("S%sE%s - %s",
                                this.getSeasonNumber(),
                                ep.getEpisodeNumber(),
                                ep.getTitle()),
                        ep.getImage());

                initializeMedia(ep);
                //castProvider.initialize(ep);
            }
        }
        else if (requestType == JukeboxDomain.RequestType.TypeMovie) {
            JukeboxDomain.Movie m = response.getMovie();

            this.setImdbUrl(m.getImdbUrl());
            this.setMediaList(m.getMediaList());
            this.setCurrentMediaIndex(0);
            initializeView(v, m.getTitle(), m.getImage());
            initializeMedia(m);
            //castProvider.initialize(m);
        }

    }

    private void initializeMedia(JukeboxDomain.Movie m) {
        currentId = m.getID();
        currentMovie = m;
        currentTitle = m.getIdentifiedTitle();
        currentEpisode = null;
    }

    private void initializeMedia(JukeboxDomain.Episode ep) {
        currentId = ep.getID();
        currentMovie = null;
        currentTitle = ep.getTitle();
        currentEpisode = ep;
    }

    protected void startMedia() {
        loadingVisible = true;
        getActivity().runOnUiThread(() -> setVisibility(getView()));

        this.getConnectionHandler()
            .startMovie(
                getPlayerName(),
                currentMovie,
                currentEpisode,
                getCallback());
    }

    protected String getPlayerName() {
        String player = JukeboxSettings.get().getCurrentMediaPlayer();
        if (StringUtils.equalsIgnoreCase(player, "local"))
            player = "ChromeCast";

        return player;
    }

    public static Fragment newInstance(boolean isLocalPlayer, boolean screenChanged) {
        Bundle b = new Bundle();
        Fragment fm = null;

        if (isLocalPlayer)
            fm = new LocalPlayerFragment();
        else if (ChromeCastConfiguration.getCastType() == JukeboxCastType.ChromeCast && ChromeCastConfiguration.isChromecastConnected())
            fm = new ChromecastPlayerFragment();
        else
            fm = new JukeboxPlayerFragment();

        b.putBoolean("screenChanged", screenChanged);

        fm.setArguments(b);

        return fm;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getConnectionHandler().getItem(
                this.getID(),
                this.getRequestType(),
                false,
                true,
                response -> {
                    initializeView(getView(), this.getRequestType(), response);
                    onGetItemCompleted();

                    getActivity().runOnUiThread(() -> {
                        setVisibility(getView());
                    });

                    if (!this.getScreenChanged())
                        startMedia();
                });
    }


    @Override
    public void onStop() {
        //save media state
        //cacheData.saveMediaState(this.getMedia().getID(), getPla);
        super.onStop();
    }

    protected void saveMediaState(int seekPosition) {
        cacheData.saveMediaState(this.getMedia().getID(), seekPosition);
    }

    public abstract void setVisibility(View v);
    public abstract void onGetItemCompleted();
    public abstract RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback();
    public abstract void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri);

    protected void showSubtitleDialog() {
        FragmentManager fm = getFragmentManager();

        SubtitleSelectFragment subtitleSelectFragment = SubtitleSelectFragment.newInstance(this.getMedia(), this);
        subtitleSelectFragment.show(fm, "subtitleSelectFragment");

    }

}
