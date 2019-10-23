package se.qxx.jukebox.comm.client;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.qxx.jukebox.comm.RpcCallback;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.*;
import se.qxx.jukebox.domain.JukeboxServiceGrpc;

import java.util.ArrayList;
import java.util.List;

public class JukeboxConnectionHandler  {
	
	private JukeboxResponseListener listener;
	private JukeboxServiceGrpc.JukeboxServiceFutureStub service;

	private JukeboxResponseListener getListener() {
		return listener;
	}

	public void setListener(JukeboxResponseListener listener) {
		this.listener = listener;
	}

	public JukeboxConnectionHandler(String serverIPaddress, int port) {
		init(serverIPaddress, port);
	}
	
	public JukeboxConnectionHandler(String serverIPaddress, int port, JukeboxResponseListener listener) {
		init(serverIPaddress, port);
		this.setListener(listener);
	}

	private void init(String serverIPaddress, int port) {
		ManagedChannel managedChannel = ManagedChannelBuilder
				.forAddress(serverIPaddress, port)
				.usePlaintext()
				.build();

		service = JukeboxServiceGrpc.newFutureStub(managedChannel);
	}
	
	//----------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------- RPC Calls
	//----------------------------------------------------------------------------------------------------------------

	public void getItem(final int id, final RequestType requestType, boolean excludeImages, boolean excludeTextData) {
		List<JukeboxDomain.RequestFilter> filter = getFilter(excludeImages, excludeTextData);

		JukeboxDomain.JukeboxRequestGetItem request = JukeboxDomain.JukeboxRequestGetItem.newBuilder()
				.setRequestType(requestType)
				.setID(id)
				.addAllFilter(filter)
				.build();

		ListenableFuture future = this.service.getItem(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}

	public void blacklist(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.blacklist(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void reIdentify(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.reIdentify(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());
	}
	
	public void startMovie(
			final String playerName, 
			final Movie m,
			final Episode ep,
			final JukeboxDomain.SubtitleRequestType subtitleRequestType) {

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
			Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

		}

	}
	
	public void stopMovie(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.stopMovie(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void pauseMovie(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.pauseMovie(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void listMovies(String searchString, int nrOfItems, int offset, boolean excludeImages, boolean excludeTextdata, final RpcCallback<JukeboxResponseListMovies> callback) {
        List<JukeboxDomain.RequestFilter> requestFilters = getFilter(excludeImages, excludeTextdata);
		list(searchString, RequestType.TypeMovie, nrOfItems, offset, requestFilters, callback);
	}
	
	public void listSeries(String searchString, int nrOfItems, int offset, boolean excludeImages, boolean excludeTextdata, final RpcCallback<JukeboxResponseListMovies> callback) {
        List<JukeboxDomain.RequestFilter> requestFilters = getFilter(excludeImages, excludeTextdata);
		list(searchString, RequestType.TypeSeries, nrOfItems, offset, requestFilters, callback);
	}

	private void list(final String searchString,
                      final RequestType type,
                      final int nrOfItems,
                      final int offset,
                      final List<JukeboxDomain.RequestFilter> requestFilters,
                      final RpcCallback<JukeboxResponseListMovies> callback) {
		JukeboxRequestListMovies request = JukeboxRequestListMovies.newBuilder()
				.setSearchString(searchString)
				.setRequestType(type)
				.setNrOfItems(nrOfItems)
				.setStartIndex(offset)
				.setReturnFullSizePictures(false)
				.addAllFilter(requestFilters)
				.build();

		ListenableFuture future = this.service.listMovies(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

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
	
	public void wakeup(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.wakeup(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}

	public void toggleFullscreen(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.toggleFullscreen(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());
	}

	public void isPlaying(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.isPlaying(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}

	public void getTime(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.getTime(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());
	}

	public void getTitle(final String playerName, final RpcCallback<JukeboxResponseGetTitle> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.getTitle(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}

	public void suspend(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		ListenableFuture future = this.service.suspend(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void listPlayers(final RpcCallback<JukeboxResponseListPlayers> callback) {
		ListenableFuture future = this.service.listPlayers(Empty.getDefaultInstance());
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void listSubtitles(final Media md, final RpcCallback<JukeboxResponseListSubtitles> callback) {
		JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
				.setMediaId(md.getID())
				.setSubtitleRequestType(JukeboxDomain.SubtitleRequestType.WebVTT)
				.build();

		ListenableFuture future = this.service.listSubtitles(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void seek(final String playerName,final int seconds) {
		JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
				.setPlayerName(playerName)
				.setSeconds(seconds)
				.build();

		ListenableFuture future = this.service.seek(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());
	}
	
	public void setSubtitle(final String playerName, final int mediaId, final Subtitle subtitle) {
		JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
				.setPlayerName(playerName)
				.setMediaID(mediaId)
				.setSubtitleDescription(subtitle.getDescription())
				.build();

		ListenableFuture future = this.service.setSubtitle(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());
	}

	public void toggleWatched(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.toggleWatched(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}
	
	public void reenlistSub(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.reenlistSubtitles(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}

	public void reenlistMetadata(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		ListenableFuture future = this.service.reenlistMetadata(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());

	}

	public void forceconversion(final int mediaId) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(mediaId)
				.setRequestType(RequestType.TypeMovie)
				.build();

		ListenableFuture future = this.service.forceConverter(request);
		Futures.addCallback(future, new RpcCallback<>(this.getListener()), MoreExecutors.directExecutor());
	}
}
