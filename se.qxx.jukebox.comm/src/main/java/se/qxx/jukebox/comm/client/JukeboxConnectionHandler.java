package se.qxx.jukebox.comm.client;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import se.qxx.jukebox.comm.EmptyRpcCallback;
import se.qxx.jukebox.comm.RpcCallback;
import se.qxx.jukebox.comm.client.JukeboxConnectionPool;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestID;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSetSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.qxx.jukebox.domain.JukeboxServiceGrpc;

public class JukeboxConnectionHandler  {
	
	private JukeboxResponseListener listener;
	private JukeboxServiceGrpc.JukeboxServiceStub service;

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
				.forAddress(serverIPaddress, port).usePlaintext().build();

		service = se.qxx.jukebox.domain.JukeboxServiceGrpc.newStub(managedChannel);
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

		this.service.getItem(request, new RpcCallback<>(this.getListener()));

	}

	public void blacklist(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		this.service.blacklist(request, new RpcCallback<>(this.getListener()));
	}
	
	public void reIdentify(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		this.service.reIdentify(request, new RpcCallback<>(this.getListener()));
	}
	
	public void startMovie(
			final String playerName, 
			final Movie m,
			final Episode ep,
			final JukeboxDomain.SubtitleRequestType subtitleRequestType,
			final RpcCallback<JukeboxResponseStartMovie> callback) {
		if (m != null || ep != null) {
			int id = m == null ? ep.getID() : m.getID();
			RequestType requestType = m == null ? RequestType.TypeEpisode : RequestType.TypeMovie;

			JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
					.setPlayerName(playerName)  // JukeboxSettings.get().getCurrentMediaPlayer()
					.setMovieOrEpisodeId(id) // Model.get().getCurrentMovie().getID()
					.setRequestType(requestType)
					.setSubtitleRequestType(subtitleRequestType)
					.build();

			this.service.startMovie(request, new RpcCallback<>(this.getListener()));
		}

	}
	
	public void stopMovie(final String playerName, final RpcCallback<Empty> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.stopMovie(request, new RpcCallback<>(this.getListener()));
	}
	
	public void pauseMovie(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.pauseMovie(request, new RpcCallback<>(this.getListener()));
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

		this.service.listMovies(request, new RpcCallback<>(this.getListener()));

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

		this.service.wakeup(request, new RpcCallback<>(this.getListener()));
	}

	public void toggleFullscreen(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.toggleFullscreen(request, new RpcCallback<>(this.getListener()));
	}

	public void isPlaying(final String playerName, final RpcCallback<JukeboxResponseIsPlaying> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.isPlaying(request, new RpcCallback<>(this.getListener()));
	}

	public void getTime(final String playerName, final RpcCallback<JukeboxResponseTime> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.getTime(request, new RpcCallback<>(this.getListener()));
	}

	public void getTitle(final String playerName, final RpcCallback<JukeboxResponseGetTitle> callback) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.getTitle(request, new RpcCallback<>(this.getListener()));
	}

	public void suspend(final String playerName) {
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.service.suspend(request, new RpcCallback<>(this.getListener()));
	}
	
	public void listPlayers(final RpcCallback<JukeboxResponseListPlayers> callback) {
		this.service.listPlayers(Empty.getDefaultInstance(), new RpcCallback<>(this.getListener()));
	}
	
	public void listSubtitles(final Media md, final RpcCallback<JukeboxResponseListSubtitles> callback) {
		JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
				.setMediaId(md.getID())
				.setSubtitleRequestType(JukeboxDomain.SubtitleRequestType.WebVTT)
				.build();

		this.service.listSubtitles(request, new RpcCallback<>(this.getListener()));
	}
	
	public void seek(final String playerName,final int seconds) {
		JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
				.setPlayerName(playerName)
				.setSeconds(seconds)
				.build();

		this.service.seek(request, new RpcCallback<>(this.getListener()));
	}
	
	public void setSubtitle(final String playerName, final int mediaId, final Subtitle subtitle) {
		JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
				.setPlayerName(playerName)
				.setMediaID(mediaId)
				.setSubtitleDescription(subtitle.getDescription())
				.build();

		this.service.setSubtitle(request, new RpcCallback<>(this.getListener()));
	}

	public void toggleWatched(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		this.service.toggleWatched(request, new RpcCallback<>(this.getListener()));
	}
	
	public void reenlistSub(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		this.service.reenlistSubtitles(request, new RpcCallback<>(this.getListener()));

	}

	public void reenlistMetadata(final int id, final RequestType requestType) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(id)
				.setRequestType(requestType)
				.build();

		this.service.reenlistMetadata(request, new RpcCallback<>(this.getListener()));
	}

	public void forceconversion(final int mediaId) {
		JukeboxRequestID request = JukeboxRequestID.newBuilder()
				.setId(mediaId)
				.setRequestType(RequestType.TypeMovie)
				.build();

		this.service.forceConverter(request, new RpcCallback<>(this.getListener()));
	}

}
