package se.qxx.android.jukebox.comm;

import android.util.Log;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.*;
import se.qxx.jukebox.domain.JukeboxServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JukeboxConnectionHandler<T>  {
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

    public JukeboxConnectionHandler(String serverIPaddress, int port) {
		init(serverIPaddress, port);
	}

	public JukeboxConnectionHandler(String serverIPaddress, int port, JukeboxResponseListener listener) {
		init(serverIPaddress, port);
		this.setListener(listener);
	}

	private void init(String serverIPaddress, int port) {
		channel = ManagedChannelBuilder
				.forAddress(serverIPaddress, port)
				.usePlaintext()
				.build();

		service = JukeboxServiceGrpc.newFutureStub(channel);
	}

    public void connect(final int offset, final int nrOfItems, ViewMode modelType, final int seriesID, int seasonID, boolean excludeImages, boolean excludeTextdata) {
        try {

            if (modelType == ViewMode.Movie) {
                Logger.Log().d("Listing movies");
                this.listMovies("",
                        nrOfItems,
                        offset,
                        excludeImages,
                        excludeTextdata,
                        response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
                                triggerMoviesUpdated(response.getMoviesList(), response.getTotalMovies());
                            }
                        });
            }
            else if (modelType == ViewMode.Series) {
                this.listSeries("",
                        nrOfItems,
                        offset,
                        excludeImages,
                        excludeTextdata,
                        response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
                                triggerSeriesUpdated(response.getSeriesList(), response.getTotalSeries());
                            }
                        });
            }
            else if (modelType == ViewMode.Season) {
                this.getItem(seriesID, JukeboxDomain.RequestType.TypeSeries, excludeImages, excludeTextdata,
                        response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
                                triggerSeasonsUpdated(
										response.getSerie().getSeasonList(),
										response.getSerie().getSeasonCount());

                            }
                        });
            }
            else if (modelType == ViewMode.Episode) {
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
			Log.e(TAG,"Error when terminating channel", e);
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

		ListenableFuture future = this.service.getItem(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}

	public void blacklist(final int id, final RequestType requestType,
						  HandlerCallback<Empty> callback) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.blacklist(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}
	
	public void reIdentify(final int id, final RequestType requestType,
						   HandlerCallback<Empty> callback) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.reIdentify(request);
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
					.setPlayerName(playerName)  // JukeboxSettings.get().getCurrentMediaPlayer()
					.setMovieOrEpisodeId(id) // Model.get().getCurrentMovie().getID()
					.setRequestType(requestType)
					.setSubtitleRequestType(subtitleRequestType)
					.build();

			ListenableFuture future = this.service.startMovie(request);
			Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

		}

	}
	
	public void stopMovie(final String playerName, HandlerCallback<Empty> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.stopMovie(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}
	
	public void pauseMovie(final String playerName, HandlerCallback<Empty> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.pauseMovie(request);
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

		ListenableFuture future = this.service.listMovies(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());


	}

	public List<JukeboxDomain.RequestFilter> getDefaultFilter() {
		List<JukeboxDomain.RequestFilter> result = new ArrayList<JukeboxDomain.RequestFilter>();
		result.add(JukeboxDomain.RequestFilter.Images);
		result.add(JukeboxDomain.RequestFilter.SubsTextdata);

		return result;
	}

    public List<JukeboxDomain.RequestFilter> getFilter(boolean excludeImages, boolean excludeTextData) {
        List<JukeboxDomain.RequestFilter> result = new ArrayList<JukeboxDomain.RequestFilter>();

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

		ListenableFuture future = this.service.wakeup(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}

	public void toggleFullscreen(final String playerName, HandlerCallback<Empty> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.toggleFullscreen(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
	}

	public void isPlaying(final String playerName, HandlerCallback<JukeboxResponseIsPlaying> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.isPlaying(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}

	public void getTime(final String playerName, HandlerCallback<JukeboxResponseTime> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.getTime(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
	}

	public void getTitle(final String playerName, HandlerCallback<JukeboxResponseGetTitle> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.getTitle(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}

	public void suspend(final String playerName, HandlerCallback<Empty> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.suspend(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}
	
	public void listPlayers(HandlerCallback<JukeboxResponseListPlayers> callback) {
		ListenableFuture future = this.service.listPlayers(Empty.getDefaultInstance());
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}
	
	public void listSubtitles(final Media md, HandlerCallback<JukeboxResponseListSubtitles> callback) {
		JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
				.setMediaId(md.getID())
				.setSubtitleRequestType(JukeboxDomain.SubtitleRequestType.WebVTT)
				.build();

		ListenableFuture future = this.service.listSubtitles(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}
	
	public void seek(final String playerName,final int seconds, HandlerCallback<Empty> callback) {
		JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
				.setPlayerName(playerName)
				.setSeconds(seconds)
				.build();

		ListenableFuture future = this.service.seek(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
	}
	
	public void setSubtitle(final String playerName, final int mediaId, final Subtitle subtitle,
							HandlerCallback<Empty> callback) {
		JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
				.setPlayerName(playerName)
				.setMediaID(mediaId)
				.setSubtitleDescription(subtitle.getDescription())
				.build();

		ListenableFuture future = this.service.setSubtitle(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
	}

	public void toggleWatched(final int id, final RequestType requestType, HandlerCallback<Empty> callback) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.toggleWatched(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}
	
	public void reenlistSub(final int id, final RequestType requestType, HandlerCallback<Empty> callback) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.reenlistSubtitles(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}

	public void reenlistMetadata(final int id, final RequestType requestType, HandlerCallback<Empty> callback) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.reenlistMetadata(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());

	}

	public void forceconversion(final int mediaId, HandlerCallback<Empty> callback) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(mediaId)
				.setRequestType(RequestType.TypeMovie)
				.build();

		ListenableFuture future = this.service.forceConverter(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener(), callback), MoreExecutors.directExecutor());
	}
}
