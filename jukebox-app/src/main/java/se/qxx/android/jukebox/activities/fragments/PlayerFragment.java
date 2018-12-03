package se.qxx.android.jukebox.activities.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.cast.CastProvider;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain;

public abstract class PlayerFragment extends Fragment implements JukeboxResponseListener {

    private boolean loadingVisible;
    private CastProvider castProvider;
    private JukeboxConnectionHandler connectionHandler;

    private List<JukeboxDomain.Media> mediaList;
    private int currentMediaIndex = 0;

    private String imdbUrl = StringUtils.EMPTY;


    private int getSeasonNumber() {
        Bundle b = getActivity().getIntent().getExtras();
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

    public JukeboxConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(JukeboxConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public CastProvider getCastProvider() {
        return castProvider;
    }

    public void setCastProvider(CastProvider castProvider) {
        this.castProvider = castProvider;
    }

    public boolean getLoadingVisible() {
        return loadingVisible;
    }

    public void setLoadingVisible(boolean loadingVisible) {
        this.loadingVisible = loadingVisible;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setConnectionHandler(
            new JukeboxConnectionHandler(
                JukeboxSettings.get().getServerIpAddress(),
                JukeboxSettings.get().getServerPort()));

        this.getConnectionHandler()
            .setListener(this);

    }

    protected void setVisibility(View v, boolean surfaceViewVisible) {
        SurfaceView sv = v.findViewById(R.id.surfaceview);

        SeekBar sb = v.findViewById(R.id.seekBarDuration);
        LinearLayout linearLayout2 = v.findViewById(R.id.linearLayout2);
        LinearLayout linearLayout1 = v.findViewById(R.id.linearLayout1);
        LinearLayout linearLayoutButtons1 = v.findViewById(R.id.linearLayoutButtons1);
        TextView txtSeekIndicator = v.findViewById(R.id.txtSeekIndicator);
        ProgressBar spinner = v.findViewById(R.id.spinner);

        boolean boolLoadingVisible = this.getLoadingVisible();

        int standardControlsMode = surfaceViewVisible || boolLoadingVisible ? View.GONE : View.VISIBLE;
        int mediaControllerMode = !surfaceViewVisible || boolLoadingVisible ? View.GONE : View.VISIBLE;
        int spinnerVisible = boolLoadingVisible ? View.VISIBLE : View.GONE;

        sb.setVisibility(standardControlsMode);
        linearLayout1.setVisibility(standardControlsMode);
        linearLayout2.setVisibility(standardControlsMode);
        linearLayoutButtons1.setVisibility(standardControlsMode);
        txtSeekIndicator.setVisibility(standardControlsMode);

        sv.setVisibility(mediaControllerMode);

        spinner.setVisibility(spinnerVisible);

    }

    protected void initializeView(final String title, final ByteString image) {
        getActivity().runOnUiThread(() -> {
            View v = GUITools.getRootView(getContext());

            if (!image.isEmpty()) {
                Bitmap bm = GUITools.getBitmapFromByteArray(image.toByteArray());
                Bitmap scaledImage = GUITools.scaleImage(300, bm, getContext());
                GUITools.setImageOnImageView(R.id.imgNowPlaying, scaledImage, v);
            }

            GUITools.setTextOnTextview(R.id.lblNowPlayingTitle, title, v);
        });

    }


    protected void initializeCastProvider(MediaPlayer.OnPreparedListener preparedListener, SeekerListener seekerListener) {
        SurfaceHolder holder = getSurfaceHolder();

        castProvider = CastProvider.getCaster(
                this.getActivity(),
                this.getConnectionHandler(),
                null,
                seekerListener,
                holder,
                preparedListener);
    }

    @Override
    public void onRequestComplete(JukeboxConnectionMessage message) {
        if (!message.result()) {
            getActivity().runOnUiThread(() -> Toast.makeText(this.getContext(),
                    "Failed :: " + message.getMessage(),
                    Toast.LENGTH_LONG).show());
        }


        loadingVisible = false;
        getActivity().runOnUiThread(() -> setVisibility(getView(), isLocalPlayer()));
    }

    private SurfaceHolder getSurfaceHolder() {
        final SurfaceView view = getView().findViewById(R.id.surfaceview);
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

    private void initializeView(JukeboxDomain.RequestType requestType, JukeboxDomain.JukeboxResponseGetItem response) {

        if (requestType == JukeboxDomain.RequestType.TypeEpisode) {
            JukeboxDomain.Episode ep = response.getEpisode();

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
            JukeboxDomain.Movie m = response.getMovie();

            this.setImdbUrl(m.getImdbUrl());
            this.setMediaList(m.getMediaList());
            this.setCurrentMediaIndex(0);
            initializeView(m.getTitle(), m.getImage());
            castProvider.initialize(m);
        }

    }

    protected void startMedia() {
        loadingVisible = true;
        setVisibility(isLocalPlayer());

        castProvider.startMovie();
    }




    public abstract boolean isLocalPlayer();

}
