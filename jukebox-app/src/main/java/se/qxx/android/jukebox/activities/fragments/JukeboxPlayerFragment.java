package se.qxx.android.jukebox.activities.fragments;


import android.app.Fragment;
import android.os.Bundle;

import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

/**
 * A simple {@link Fragment} subclass.
 */
public class JukeboxPlayerFragment extends RemotePlayerFragment {


    public JukeboxPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /***
     * When activity starts it launches a call to the server checking if something
     * is playing on the remote player
     */
    @Override
    public void onStart() {
        super.onStart();

        initializeJukeboxCast();
    }

    /***
     * Makes call to server to check if something is playing
     */
    private void initializeJukeboxCast() {
        Logger.Log().d("Request -- IsPlaying");
        this.getConnectionHandler().isPlaying(
                getSettings().getCurrentMediaPlayer(),
                new OnStatusComplete());
    }

    /***
     * Callback from initial isPlaying.
     * If something is playing then make call to server to get whats playing
     * otherwise start the new movie
     */
    private class OnStatusComplete implements RpcCallback<JukeboxDomain.JukeboxResponseIsPlaying> {
        @Override
        public void run(JukeboxDomain.JukeboxResponseIsPlaying response) {
            Logger.Log().d("Response --- IsPlaying");
            if (response != null) {
                if (response.getIsPlaying()) {
                    Logger.Log().d("Request --- GetTitle");
                    getConnectionHandler().getTitle(getSettings().getCurrentMediaPlayer(), new OnGetTitleComplete());
                } else {
                    Logger.Log().d("Request --- StartMovie");
                    startMedia();
                }
            }
        }
    }

    /***
     * Callback from the get title call
     * If the title is the same as the one selected then continue playing
     * Otherwise stop movie and start the new one in the callback
     */
    private class OnGetTitleComplete implements RpcCallback<JukeboxDomain.JukeboxResponseGetTitle> {
        @Override
        public void run(JukeboxDomain.JukeboxResponseGetTitle response) {
            Logger.Log().d("Response --- GetTitle");
            if (response != null) {
                String playerFilename = response.getTitle();
                int mediaIndex = matchCurrentFilenameAgainstMedia(playerFilename);
                if (mediaIndex >= 0) {
                    //initialize seeker and get subtitles if app has been reinitialized
                    setCurrentMediaIndex(mediaIndex);
                    setDuration(getMedia().getMetaDuration());

                    //Start seeker and get time asap as the movie is playing
                    startSeekerTimer();

                    //initializeSubtitles();
                }
            } else {
                Thread t2 = new Thread(() -> {
                    Logger.Log().d("Request --- StopMovie");
                    getConnectionHandler().stopMovie(
                            getSettings().getCurrentMediaPlayer(),
                            new OnStopMovieComplete());
                });
                t2.start();
            }
        }
    }

    /***
     * Matches a filename with the current media
     * @param playerFilename remote filename to match against
     * @return mediaIndex if found. -1 if not found
     */
    protected int matchCurrentFilenameAgainstMedia(String playerFilename) {
        for (int i = 0; i < this.getMediaList().size(); i++) {
            if (StringUtils.equalsIgnoreCase(playerFilename, this.getMediaList().get(i).getFilename())) {
                return i;
            }
        }

        return -1;
    }

    /***
     * Callback to stop media
     * start the new media
     */
    private class OnStopMovieComplete implements RpcCallback<JukeboxDomain.Empty> {
        @Override
        public void run(JukeboxDomain.Empty arg0) {
            Logger.Log().d("Response --- StopMovie");

            startMedia();
        }
    }

    @Override
    public void onStartMovieComplete(JukeboxDomain.JukeboxResponseStartMovie response) {
        Logger.Log().d("Response --- StartMovie");

        setDuration(this.getMedia().getMetaDuration());
        startSeekerTimer();
    }

    @Override
    public void setSubtitle(JukeboxDomain.SubtitleUri subtitleUri) {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {
        String player = getSettings().getCurrentMediaPlayer();

        stopSeekerTimer();
        this.getConnectionHandler().stopMovie(player, null);
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        this.getConnectionHandler().seek(
                getSettings().getCurrentMediaPlayer(),
                pos);
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    public JukeboxDomain.SubtitleRequestType getSubtitleRequestType() {
        return JukeboxDomain.SubtitleRequestType.SubRip;
    }
}
