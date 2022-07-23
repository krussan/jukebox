package se.qxx.android.jukebox.comm;

import android.util.Log;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestID;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSetSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetItem;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxServiceGrpc;

public class JukeboxConnectionHandler {
    private static final String TAG = "JukeboxConnectionHandler";

    private List<ConnectorCallbackEventListener> callbacks = new ArrayList<>();
    private JukeboxResponseListener listener;
    private JukeboxServiceGrpc.JukeboxServiceFutureStub service;
    private ManagedChannel channel;

    private JukeboxResponseListener getListener() {
        return listener;
    }

    public interface ConnectorCallbackEventListener {
        void handleMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies);

        void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries);

        void handleSeasonsUpdated(List<JukeboxDomain.Season> seasons, int totalSeasons);

        void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes, int totalEpisodes);
    }

    public void setListener(JukeboxResponseListener listener) {
        this.listener = listener;
    }

    public List<ConnectorCallbackEventListener> getCallbacks() {
        return this.callbacks;
    }

    public void addCallback(ConnectorCallbackEventListener callback) {
        this.callbacks.add(callback);
    }

    public void removeCallback(ConnectorCallbackEventListener callback) {
        this.callbacks.remove(callback);
    }

    public JukeboxConnectionHandler(String serverIPaddress, int port) {
        init(serverIPaddress, port);
    }

    private void init(String serverIPaddress, int port) {
        channel = ManagedChannelBuilder
                .forAddress(serverIPaddress, port)
                .usePlaintext()
                .build();

        service = JukeboxServiceGrpc.newFutureStub(channel);
    }

    public void connect(String searchString, final int offset, final int nrOfItems, ViewMode modelType, final int seriesID, int seasonID, boolean excludeImages, boolean excludeTextdata) {
        try {

            if (modelType == ViewMode.Movie) {
                Logger.Log().d("Listing movies");
                this.listMovies(searchString,
                        nrOfItems,
                        offset,
                        excludeImages,
                        excludeTextdata,
                        response -> {
                            if (response != null) {
                                triggerMoviesUpdated(response.getMoviesList(), response.getTotalMovies());
                            }
                        });
            } else if (modelType == ViewMode.Series) {
                this.listSeries(searchString,
                        nrOfItems,
                        offset,
                        excludeImages,
                        excludeTextdata,
                        response -> {
                            if (response != null) {
                                triggerSeriesUpdated(response.getSeriesList(), response.getTotalSeries());
                            }
                        });
            } else if (modelType == ViewMode.Season) {
                this.getItem(seriesID, JukeboxDomain.RequestType.TypeSeries, excludeImages, excludeTextdata,
                        response -> {
                            if (response != null) {
                                triggerSeasonsUpdated(
                                        response.getSerie().getSeasonList(),
                                        response.getSerie().getSeasonCount());

                            }
                        });
            } else if (modelType == ViewMode.Episode) {
                this.getItem(seasonID, JukeboxDomain.RequestType.TypeSeason, excludeImages, excludeTextdata,
                        response -> {
                            if (response != null) {
                                triggerEpisodesUpdated(
                                        response.getSeason().getEpisodeList(),
                                        response.getSeason().getEpisodeCount());
                            }
                        });
            }

        } catch (Exception e) {
            Logger.Log().e("Error when connecting to server", e);
        }
    }

    private void triggerEpisodesUpdated(List<Episode> episodeList, int totalEpisodeCount) {
        for (ConnectorCallbackEventListener listener : this.getCallbacks())
            listener.handleEpisodesUpdated(episodeList, totalEpisodeCount);
    }

    private void triggerSeasonsUpdated(List<Season> seasonList, int seasonCount) {
        for (ConnectorCallbackEventListener listener : this.getCallbacks())
            listener.handleSeasonsUpdated(seasonList, seasonCount);
    }

    private void triggerSeriesUpdated(List<Series> seriesList, int totalSeries) {
        for (ConnectorCallbackEventListener listener : this.getCallbacks())
            listener.handleSeriesUpdated(seriesList, totalSeries);
    }

    private void triggerMoviesUpdated(List<Movie> moviesList, int nrOfMovies) {
        for (ConnectorCallbackEventListener listener : this.getCallbacks())
            listener.handleMoviesUpdated(moviesList, nrOfMovies);
    }


    public void stop() {

        try {
            channel.shutdown();
            channel.awaitTermination(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error when terminating channel", e);
        }
    }

    //----------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------- RPC Calls
    //----------------------------------------------------------------------------------------------------------------

    public void getItem(final int id, final RequestType requestType, boolean excludeImages, boolean excludeTextData,
                        HandlerCallback<JukeboxResponseGetItem> callback) {
        List<JukeboxDomain.RequestFilter> filter = getFilter(excludeImages, excludeTextData);

        JukeboxDomain.JukeboxRequestGetItem request = JukeboxDomain.JukeboxRequestGetItem.newBuilder()
                .setRequestType(requestType)
                .setID(id)
                .addAllFilter(filter)
                .build();

        ListenableFuture<JukeboxResponseGetItem> future = this.service.getItem(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void blacklist(final int id, final RequestType requestType,
                          HandlerCallback<Empty> callback) {
        JukeboxRequestID request = JukeboxRequestID.newBuilder()
                .setId(id)
                .setRequestType(requestType)
                .build();

        ListenableFuture<Empty> future = this.service.blacklist(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void reIdentify(final int id, final RequestType requestType,
                           HandlerCallback<Empty> callback) {
        JukeboxRequestID request = JukeboxRequestID.newBuilder()
                .setId(id)
                .setRequestType(requestType)
                .build();

        ListenableFuture<Empty> future = this.service.reIdentify(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
    }

    public void startMovie(
            final String playerName,
            final Movie m,
            final Episode ep,
            final JukeboxDomain.SubtitleRequestType subtitleRequestType,
            final HandlerCallback<JukeboxResponseStartMovie> callback) {

        if (m != null || ep != null) {
            int id = m == null ? ep.getID() : m.getID();
            RequestType requestType = m == null ? RequestType.TypeEpisode : RequestType.TypeMovie;

            JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
                    .setPlayerName(playerName)
                    .setMovieOrEpisodeId(id)
                    .setRequestType(requestType)
                    .setSubtitleRequestType(subtitleRequestType)
                    .build();

            ListenableFuture<JukeboxResponseStartMovie> future = this.service.startMovie(request);
            Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

        }

    }

    public void stopMovie(final String playerName, HandlerCallback<Empty> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<Empty> future = this.service.stopMovie(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void pauseMovie(final String playerName, HandlerCallback<Empty> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<Empty> future = this.service.pauseMovie(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void listMovies(String searchString, int nrOfItems, int offset, boolean excludeImages, boolean excludeTextdata, HandlerCallback<JukeboxResponseListMovies> callback) {
        List<JukeboxDomain.RequestFilter> requestFilters = getFilter(excludeImages, excludeTextdata);
        list(searchString, RequestType.TypeMovie, nrOfItems, offset, requestFilters, callback);
    }

    public void listSeries(String searchString, int nrOfItems, int offset, boolean excludeImages, boolean excludeTextdata, HandlerCallback<JukeboxResponseListMovies> callback) {
        List<JukeboxDomain.RequestFilter> requestFilters = getFilter(excludeImages, excludeTextdata);
        list(searchString, RequestType.TypeSeries, nrOfItems, offset, requestFilters, callback);
    }

    private void list(final String searchString,
                      final RequestType type,
                      final int nrOfItems,
                      final int offset,
                      final List<JukeboxDomain.RequestFilter> requestFilters,
                      final HandlerCallback<JukeboxResponseListMovies> callback) {

        JukeboxRequestListMovies request = JukeboxRequestListMovies.newBuilder()
                .setSearchString(searchString)
                .setRequestType(type)
                .setNrOfItems(nrOfItems)
                .setStartIndex(offset)
                .setReturnFullSizePictures(false)
                .addAllFilter(requestFilters)
                .build();

        ListenableFuture<JukeboxResponseListMovies> future = this.service.listMovies(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());


    }

    public List<JukeboxDomain.RequestFilter> getDefaultFilter() {
        List<JukeboxDomain.RequestFilter> result = new ArrayList<JukeboxDomain.RequestFilter>();
        result.add(JukeboxDomain.RequestFilter.Images);
        result.add(JukeboxDomain.RequestFilter.SubsTextdata);

        return result;
    }

    public List<JukeboxDomain.RequestFilter> getFilter(boolean excludeImages, boolean excludeTextData) {
        List<JukeboxDomain.RequestFilter> result = new ArrayList<>();

        if (excludeImages)
            result.add(JukeboxDomain.RequestFilter.Images);

        if (excludeTextData)
            result.add(JukeboxDomain.RequestFilter.SubsTextdata);

        return result;
    }

    public void wakeup(final String playerName, HandlerCallback<Empty> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<Empty> future = this.service.wakeup(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void toggleFullscreen(final String playerName, HandlerCallback<Empty> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<Empty> future = this.service.toggleFullscreen(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
    }

    public void isPlaying(final String playerName, HandlerCallback<JukeboxResponseIsPlaying> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<JukeboxResponseIsPlaying> future = this.service.isPlaying(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void getTime(final String playerName, HandlerCallback<JukeboxResponseTime> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<JukeboxResponseTime> future = this.service.getTime(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
    }

    public void getTitle(final String playerName, HandlerCallback<JukeboxResponseGetTitle> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<JukeboxResponseGetTitle> future = this.service.getTitle(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void suspend(final String playerName, HandlerCallback<Empty> callback) {
        JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                .setPlayerName(playerName)
                .build();

        ListenableFuture<Empty> future = this.service.suspend(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void listPlayers(HandlerCallback<JukeboxResponseListPlayers> callback) {
        ListenableFuture<JukeboxResponseListPlayers> future = this.service.listPlayers(Empty.getDefaultInstance());
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void listSubtitles(final Media md, HandlerCallback<JukeboxResponseListSubtitles> callback) {
        JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
                .setMediaId(md.getID())
                .setSubtitleRequestType(JukeboxDomain.SubtitleRequestType.WebVTT)
                .build();

        ListenableFuture<JukeboxResponseListSubtitles> future = this.service.listSubtitles(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void seek(final String playerName, final int seconds, HandlerCallback<Empty> callback) {
        JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
                .setPlayerName(playerName)
                .setSeconds(seconds)
                .build();

        ListenableFuture<Empty> future = this.service.seek(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
    }

    public void setSubtitle(final String playerName, final int mediaId, final Subtitle subtitle,
                            HandlerCallback<Empty> callback) {
        JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
                .setPlayerName(playerName)
                .setMediaID(mediaId)
                .setSubtitleDescription(subtitle.getDescription())
                .build();

        ListenableFuture<Empty> future = this.service.setSubtitle(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
    }

    public void toggleWatched(final int id, final RequestType requestType, HandlerCallback<Empty> callback) {
        JukeboxRequestID request = JukeboxRequestID.newBuilder()
                .setId(id)
                .setRequestType(requestType)
                .build();

        ListenableFuture<Empty> future = this.service.toggleWatched(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void reenlistSub(final int id, final RequestType requestType, HandlerCallback<Empty> callback) {
        JukeboxRequestID request = JukeboxRequestID.newBuilder()
                .setId(id)
                .setRequestType(requestType)
                .build();

        ListenableFuture<Empty> future = this.service.reenlistSubtitles(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void reenlistMetadata(final int id, final RequestType requestType, HandlerCallback<Empty> callback) {
        JukeboxRequestID request = JukeboxRequestID.newBuilder()
                .setId(id)
                .setRequestType(requestType)
                .build();

        ListenableFuture<Empty> future = this.service.reenlistMetadata(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

    }

    public void forceconversion(final int mediaId, HandlerCallback<Empty> callback) {
        JukeboxRequestID request = JukeboxRequestID.newBuilder()
                .setId(mediaId)
                .setRequestType(RequestType.TypeMovie)
                .build();

        ListenableFuture<Empty> future = this.service.forceConverter(request);
        Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
    }
}
