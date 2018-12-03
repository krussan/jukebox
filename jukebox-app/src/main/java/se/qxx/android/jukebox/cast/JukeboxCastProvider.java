package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.view.SurfaceView;

import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

class JukeboxCastProvider extends CastProvider {

    @Override
    public RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback() {
        return parameter -> {
            Logger.Log().d("Response --- StartMovie");
            initializeSubtitles();

            getSeekerListener().setDuration(this.getCurrentMedia().getMetaDuration());
            getSeekerListener().startSeekerTimer();
        };
    }

    @Override
    public void stop() {
        String player = JukeboxSettings.get().getCurrentMediaPlayer();

        this.getSeekerListener().stopSeekerTimer();
        this.getJukeboxConnectionHandler().stopMovie(player,null);
    }

    @Override
    public boolean usesMediaController() {
        return false;
    }

    @Override
    public void startMovie() {
        initializeJukeboxCast();
    }

    @Override
    public void initialize() {
        this.getSeekerListener().initializeSeeker();
    }

    private void startJukeboxCastMovie() {
        super.startMovie();
    }

    private void initializeJukeboxCast() {
        Logger.Log().d("Request -- IsPlaying");
        this.getJukeboxConnectionHandler().isPlaying(
                JukeboxSettings.get().getCurrentMediaPlayer(),
                new OnStatusComplete());
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

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
    public void seekTo(int position) {
        this.getJukeboxConnectionHandler().seek(
                JukeboxSettings.get().getCurrentMediaPlayer(),
                position);
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
    public int getAudioSessionId() {
        return 0;
    }

    private class OnStatusComplete implements RpcCallback<JukeboxDomain.JukeboxResponseIsPlaying> {
        @Override
        public void run(JukeboxDomain.JukeboxResponseIsPlaying response) {
            Logger.Log().d("Response --- IsPlaying");
            if (response != null) {
                if (response.getIsPlaying()) {
                    Logger.Log().d("Request --- GetTitle");
                    getJukeboxConnectionHandler().getTitle(JukeboxSettings.get().getCurrentMediaPlayer(), new OnGetTitleComplete());
                } else {
                    Logger.Log().d("Request --- StartMovie");
                    startMovie();
                }
            }
        }
    }

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
                    getSeekerListener().setDuration(getCurrentMedia().getMetaDuration());

                    //Start seeker and get time asap as the movie is playing
                    getSeekerListener().startSeekerTimer();

                    initializeSubtitles();
                }
            } else {
                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.Log().d("Request --- StopMovie");
                        getJukeboxConnectionHandler().stopMovie(
                                JukeboxSettings.get().getCurrentMediaPlayer(),
                                new OnStopMovieComplete());
                    }
                });
                t2.start();
            }
        }
    }

    private class OnStopMovieComplete implements RpcCallback<JukeboxDomain.Empty> {
        @Override
        public void run(JukeboxDomain.Empty arg0) {
            Logger.Log().d("Response --- StopMovie");

            startJukeboxCastMovie();
        }
    }


    protected int matchCurrentFilenameAgainstMedia(String playerFilename) {
        for (int i = 0; i < this.getMediaList().size(); i++) {
            if (StringUtils.equalsIgnoreCase(playerFilename, this.getMediaList().get(i).getFilename())) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void surfaceCreated(SurfaceView view) {

    }

    @Override
    public void surfaceDestroyed() {

    }

}
