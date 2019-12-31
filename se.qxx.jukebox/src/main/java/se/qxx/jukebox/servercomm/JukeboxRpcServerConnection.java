package se.qxx.jukebox.servercomm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGetItem;
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
import se.qxx.jukebox.domain.JukeboxDomain.RequestFilter;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleRequestType;
import se.qxx.jukebox.domain.JukeboxServiceGrpc;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IJukeboxRpcServerConnection;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Players.Server;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.webserver.StreamingFile;
import se.qxx.protodb.Logger;

public class JukeboxRpcServerConnection extends JukeboxServiceGrpc.JukeboxServiceImplBase implements IJukeboxRpcServerConnection {
	private IDatabase database;
	private ISettings settings;
	private IDistributor distributor;
	private IStreamingWebServer webServer;
	private ISubtitleDownloader subtitleDownloader;
	private IMovieIdentifier movieIdentifier;
	private IJukeboxLogger log;
	private IExecutor executor;
	private IUtils utils;
	
	@Inject
	public JukeboxRpcServerConnection(
			ISettings settings, 
			IDatabase database, 
			IDistributor distributor,
			ISubtitleDownloader subtitleDownloader,
			IMovieIdentifier movieIdentifier, 
			LoggerFactory loggerFactory,
			IExecutor executor,
			IUtils utils,
			@Assisted("webserver") IStreamingWebServer webServer) {
		super();
		this.setDatabase(database);
		this.setSettings(settings);
		this.setDistributor(distributor);
		this.setWebServer(webServer);
		this.setSubtitleDownloader(subtitleDownloader);
		this.setMovieIdentifier(movieIdentifier);
		this.setExecutor(executor);
		this.setLog(loggerFactory.create(LogType.COMM));
		this.setUtils(utils);
	}
	
	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public IMovieIdentifier getMovieIdentifier() {
		return movieIdentifier;
	}

	public void setMovieIdentifier(IMovieIdentifier movieIdentifier) {
		this.movieIdentifier = movieIdentifier;
	}

	public ISubtitleDownloader getSubtitleDownloader() {
		return subtitleDownloader;
	}

	public void setSubtitleDownloader(ISubtitleDownloader subtitleDownloader) {
		this.subtitleDownloader = subtitleDownloader;
	}

	public IStreamingWebServer getWebServer() {
		return webServer;
	}

	public void setWebServer(IStreamingWebServer webServer) {
		this.webServer = webServer;
	}

	public IDistributor getDistributor() {
		return distributor;
	}

	public void setDistributor(IDistributor distributor) {
		this.distributor = distributor;
	}

	public ISettings getSettings() {
		return settings;
	}
	public void setSettings(ISettings settings) {
		this.settings = settings;
	}
	public IDatabase getDatabase() {
		return database;
	}
	public void setDatabase(IDatabase database) {
		this.database = database;
	}
	public IExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(IExecutor executor) {
		this.executor = executor;
	}

	@Override
	public void listMovies(JukeboxRequestListMovies request, StreamObserver<JukeboxResponseListMovies> responseObserver) {
		setPriority();
		this.getLog().Debug(String.format("ListMovies :: %s", request.getRequestType()));

		String searchString = request.getSearchString();

		JukeboxResponseListMovies.Builder b = JukeboxResponseListMovies.newBuilder();
		boolean excludeImages = request.getFilterList().stream().anyMatch(r -> r == RequestFilter.Images);
		boolean excludeTextData = request.getFilterList().stream().anyMatch(r -> r == RequestFilter.SubsTextdata);

		switch (request.getRequestType()) {
			case TypeMovie:
				b.addAllMovies(
						this.getDatabase().searchMoviesByTitle(
								searchString,
								request.getNrOfItems(),
								request.getStartIndex(),
								excludeImages,
								excludeTextData));

				b.setTotalMovies(
						this.getDatabase().getTotalNrOfMovies());

				break;
			case TypeSeries:
				b.addAllSeries(
						this.getDatabase().searchSeriesByTitle(
								searchString,
								request.getNrOfItems(),
								request.getStartIndex(),
								excludeImages));

				b.setTotalSeries(
						this.getDatabase().getTotalNrOfSeries());

				break;
			default:
				break;
		}

		JukeboxResponseListMovies response = b.build();
		logResponse(response);

		responseObserver.onNext(b.build());
		responseObserver.onCompleted();
	}

	private void logResponse(JukeboxResponseListMovies response) {
		this.getLog().Debug(String.format("Movie count :: %s ", response.getMoviesCount()));
		this.getLog().Debug(String.format("Series count :: %s ", response.getSeriesCount()));
		logSeasons(response);
		logEpisodes(response);
	}

	private void logSeasons(JukeboxResponseListMovies response) {
		int c = 0;
		for (Series s : response.getSeriesList())
			c += s.getSeasonCount();
		
		this.getLog().Debug(String.format("Season count :: %s ", c));
	}
	
	private void logEpisodes (JukeboxResponseListMovies response) {
		int c = 0;
		for (Series s : response.getSeriesList()) {
			for (Season ss : s.getSeasonList()) {
				c += ss.getEpisodeCount();
			}
		}
		
		this.getLog().Debug(String.format("Episode count :: %s ", c));
	}

	@Override
	public void listPlayers(Empty request,
			StreamObserver<JukeboxResponseListPlayers> responseObserver) {

		setPriority();
		this.getLog().Debug("ListPlayers");

		Collection<String> hostnames = new ArrayList<String>();
		for (Server s : this.getSettings().getSettings().getPlayers().getServer()) {
			hostnames.add(s.getName());
		}
		
		JukeboxResponseListPlayers lp = JukeboxResponseListPlayers.newBuilder().addAllHostname(hostnames).build();

		responseObserver.onNext(lp);
		responseObserver.onCompleted();
	}

	@Override
	public void startMovie(JukeboxRequestStartMovie request,  StreamObserver<JukeboxResponseStartMovie> responseObserver) {

		setPriority();
		this.getLog().Debug("StartMovie");
		this.getLog().Debug(String.format("Starting %s with ID: %s on player %s", request.getRequestType(), request.getMovieOrEpisodeId(), request.getPlayerName()));
		
		try {
			Media md = getMedia(request);

			boolean success = true;

			// always serve the file and subtitles
			List<String> subtitleUris = serveSubtitles(md, request.getSubtitleRequestType());
			StreamingFile streamingFile = this.getWebServer().registerFile(md);
			
			// call the distributor if the player is not chromecast or local
			if (!StringUtils.equalsIgnoreCase("Chromecast", request.getPlayerName()) &&
					!StringUtils.equalsIgnoreCase("local", request.getPlayerName())) {
				success = this.getDistributor().startMovie(request.getPlayerName(), md);
			}
			
			if (success) {
				List<Subtitle> subs = md.getSubsList();
				
				JukeboxResponseStartMovie.Builder b = JukeboxResponseStartMovie.newBuilder()
						.addAllSubtitle(subs)
						.addAllSubtitleUris(subtitleUris);
				
				if (streamingFile != null) {
					b.setUri(streamingFile.getUri())
						.setMimeType(streamingFile.getMimeType());
				}

				responseObserver.onNext(b.build());
				responseObserver.onCompleted();
				
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}

		} catch (VLCConnectionNotFoundException e) {
			this.getLog().Error("Error occured when starting movie", e);
			responseObserver.onError(e);
			responseObserver.onCompleted();
		}
		
	}

	private Media getMedia(JukeboxRequestStartMovie request) {
		if (request.getRequestType() == RequestType.TypeMovie) {
			return this.getDatabase().getMovie(request.getMovieOrEpisodeId()).getMedia(0);
		}
		else if (request.getRequestType() == RequestType.TypeEpisode) {
			return this.getDatabase().getEpisode(request.getMovieOrEpisodeId()).getMedia(0);
		}
		
		return null;
	}
	
	private List<String> serveSubtitles(Media md, SubtitleRequestType subtitleRequestType) {
		List<String> subtitleUris = new ArrayList<>();
		
		this.getLog().Debug(String.format("Number of subtitles :: %s", md.getSubsCount()));
		for (Subtitle s : md.getSubsList()) {
			StreamingFile subFile = this.getWebServer().registerSubtitle(s, subtitleRequestType);
			
			if (subFile != null)
				subtitleUris.add(subFile.getUri());
		}
		
		return subtitleUris;
	}

	@Override
	public void stopMovie(JukeboxRequestGeneral request, StreamObserver<Empty> responseObserver) {

		setPriority();
		this.getLog().Debug("StopMovie");
		this.getLog().Debug(String.format("Stopping movie on player %s", request.getPlayerName()));
		
		try {
			if (this.getDistributor().stopMovie(request.getPlayerName())) {
				responseObserver.onNext(Empty.newBuilder().build());
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		

	}

	@Override
	public void pauseMovie(JukeboxRequestGeneral request, StreamObserver<Empty> responseObserver) {

		setPriority();
		this.getLog().Debug("PauseMovie");
		this.getLog().Debug(String.format("Pausing movie on player %s", request.getPlayerName()));
		
		try {
			if (this.getDistributor().pauseMovie(request.getPlayerName())) {
				responseObserver.onNext(Empty.newBuilder().build());
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		
		
	}

	@Override
	public void seek(JukeboxRequestSeek request, StreamObserver<Empty> responseObserver) {

		setPriority();
		this.getLog().Debug(String.format("Seeking on player %s to %s seconds", request.getPlayerName(), request.getSeconds()));
		
		try {
			if (this.getDistributor().seek(request.getPlayerName(), request.getSeconds())) {
				responseObserver.onNext(Empty.newBuilder().build());
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		
	}

	@Override
	public void switchVRatio(JukeboxRequestGeneral request, StreamObserver<Empty> responseObserver) {

		setPriority();
		this.getLog().Debug("SwitchVRatio");
		this.getLog().Debug(String.format("Toggling vratio on %s...", request.getPlayerName()));
		
		try {
			if (this.getDistributor().toggleVRatio(request.getPlayerName())){
				responseObserver.onNext(Empty.newBuilder().build());
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		

	}

	@Override
	public void getTime(JukeboxRequestGeneral request, StreamObserver<JukeboxResponseTime> responseObserver) {

		setPriority();
		this.getLog().Debug("GetTime");
		this.getLog().Debug(String.format("Getting time on %s...", request.getPlayerName()));
		
		try {
			String response = this.getDistributor().getTime(request.getPlayerName());
			if (response.equals(StringUtils.EMPTY)){
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
			else {
				int seconds = Integer.parseInt(response);
				String titleFilename = getTitleFilename(request.getPlayerName());
				
				JukeboxResponseTime time = JukeboxResponseTime.newBuilder()
						.setSeconds(seconds)
						.setFilename(titleFilename)
						.build();

				responseObserver.onNext(time);
				responseObserver.onCompleted();
			}
				
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		
		
	}

	@Override
	public void isPlaying(JukeboxRequestGeneral request, StreamObserver<JukeboxResponseIsPlaying> responseObserver) {

		setPriority();
		this.getLog().Debug("IsPlaying");
		this.getLog().Debug(String.format("Getting is playing status on %s...", request.getPlayerName()));
		
		try {
			boolean isPlaying = this.getDistributor().isPlaying(request.getPlayerName());

			JukeboxResponseIsPlaying r = JukeboxResponseIsPlaying.newBuilder().setIsPlaying(isPlaying).build();	

			responseObserver.onNext(r);
			responseObserver.onCompleted();

		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}				
	}

	@Override
	public void getTitle(JukeboxRequestGeneral request, StreamObserver<JukeboxResponseGetTitle> responseObserver) {
		setPriority();
		this.getLog().Debug("GetTitle -- EMPTY");
	}

	@Override
	public void blacklist(JukeboxRequestID request, StreamObserver<Empty> responseObserver) {
		
		setPriority();
		this.getLog().Debug("Blacklist -- EMPTY");

		if (request.getRequestType() == RequestType.TypeMovie) {
			Movie m = this.getDatabase().getMovie(request.getId());
			if (m != null)
				this.getDatabase().addToBlacklist(m);
		}
		
		if (request.getRequestType() == RequestType.TypeEpisode) {
			this.getLog().Debug("Not Implemented yet -- Blacklist of episodes");
		}

		responseObserver.onNext(Empty.newBuilder().build());
		responseObserver.onCompleted();
	}

	@Override
	public void toggleWatched(JukeboxRequestID request, StreamObserver<Empty> responseObserver) {
		// TODO Auto-generated method stub
		
		setPriority();
		this.getLog().Debug("ToggleWatched -- EMPTY");
	}

	@Override
	public void listSubtitles(JukeboxRequestListSubtitles request,
							  StreamObserver<JukeboxResponseListSubtitles> responseObserver) {

		setPriority();
		this.getLog().Debug(String.format("Getting list of subtitles for media ID :: %s", request.getMediaId()));
		
		Media media = this.getDatabase().getMediaById(request.getMediaId());
		
		JukeboxResponseListSubtitles.Builder b = 
			JukeboxResponseListSubtitles.newBuilder()
				.addAllSubtitleUris(
					this.getWebServer()
						.getSubtitleUris(media, request.getSubtitleRequestType()));

		responseObserver.onNext(b.build());
		responseObserver.onCompleted();
	}

	@Override
	public void setSubtitle(JukeboxRequestSetSubtitle request, StreamObserver<Empty> responseObserver) {
		
		setPriority();
		this.getLog().Debug(String.format("Setting subtitle on %s...", request.getPlayerName()));
		
		int mediaID = request.getMediaID();
		Movie m = this.getDatabase().getMovieByMediaID(mediaID);
		
		Media md = null;
		for (int i=0;i<m.getMediaCount();i++) {
			md = m.getMedia(i);
			if (md.getID() == mediaID)
				break;
		}		
		
		// It appears that VLC RC interface only reads the first sub-file option specified
		// in the command sent. Thus we need to clear playlist and restart video each time we
		// change the subtitle track.
		Subtitle subTrack = getSubtitleTrack(request.getSubtitleDescription(), md.getSubsList());
		
		//VLCDistributor.restart()
		
		try {
			if (subTrack != null) {
				if (this.getDistributor().restartWithSubtitle(
						request.getPlayerName(), 
						md, 
						subTrack.getFilename(), 
						true)) {
					responseObserver.onNext(Empty.newBuilder().build());
					responseObserver.onCompleted();
				}
				else {
					responseObserver.onError(new Exception("Error occured when connecting to target media player"));
					responseObserver.onCompleted();
				}
			}
			else {
				responseObserver.onError(new Exception("Subtitle track with that description was not found"));
				responseObserver.onCompleted();

			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		
		
	}

	@Override
	public void wakeup(JukeboxRequestGeneral request, StreamObserver<Empty> responseObserver) {

		setPriority();
		this.getLog().Debug(String.format("Waking up player %s", request.getPlayerName()));
		
		try {
			if (this.getDistributor().wakeup(request.getPlayerName())) {
				responseObserver.onNext(Empty.newBuilder().build());
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}
				
		
	}

	@Override
	public void suspend(JukeboxRequestGeneral request, StreamObserver<Empty> responseObserver) {
		setPriority();
		this.getLog().Debug(String.format("Suspending computer with player %s...", request.getPlayerName()));
		
		try {
			if (this.getDistributor().suspend(request.getPlayerName())) {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}				
		
	}

	@Override
	public void toggleFullscreen(JukeboxRequestGeneral request, StreamObserver<Empty> responseObserver) {

		setPriority();
		this.getLog().Debug(String.format("Toggling fullscreen...", request.getPlayerName()));
		
		try {
			if (this.getDistributor().toggleFullscreen(request.getPlayerName())) {
				responseObserver.onNext(Empty.newBuilder().build());
				responseObserver.onCompleted();
			}
			else {
				responseObserver.onError(new Exception("Error occured when connecting to target media player"));
				responseObserver.onCompleted();
			}
		} catch (VLCConnectionNotFoundException e) {
			responseObserver.onError(new Exception("Error occured when connecting to target media player"));
			responseObserver.onCompleted();
		}		
		
	}

	
	protected Subtitle getSubtitleTrack(String description, List<Subtitle> subs) {
		for (int i=0;i<subs.size();i++) {
			String subDescription = subs.get(i).getDescription();
			if (StringUtils.equalsIgnoreCase(subDescription, description)) {
				return subs.get(i);
			}
		}
		return null;
	}	
	 
	private String getTitleFilename(String playerName) {
		try {
			return StringUtils.trim(this.getDistributor().getTitle(playerName));
		} catch (VLCConnectionNotFoundException e) {
			return StringUtils.EMPTY; 			
		}		
	}

	@Override
	public void reIdentify(JukeboxRequestID request, StreamObserver<Empty> responsObserver) {
		
		setPriority();
		this.getLog().Debug("Re-identify -- EMPTY");

		Runnable r = () -> {
			try {
				if (request.getRequestType() == RequestType.TypeMovie) {
					Movie m = this.getDatabase().getMovie(request.getId());
					this.getDatabase().delete(m);
					reenlist(m);
				}
				
				if (request.getRequestType() == RequestType.TypeSeries) {
					Series s = this.getDatabase().getSeries(request.getId());
					this.getDatabase().delete(s);			
					reenlist(s);			
				}

				if (request.getRequestType() == RequestType.TypeSeason) {
					Season sn = this.getDatabase().getSeason(request.getId());
					this.getDatabase().delete(sn);			
					reenlist(sn);			
				}

				if (request.getRequestType() == RequestType.TypeEpisode) {
					Episode ep = this.getDatabase().getEpisode(request.getId());
					this.getDatabase().delete(ep);			
					reenlist(ep);			
				}
				
				
			}
			catch (Exception e) {
				this.getLog().Error("Error occured when deleting object from database");
			}			
		};
		this.getExecutor().start(r);

		responsObserver.onNext(Empty.newBuilder().build());
		responsObserver.onCompleted();
	}
	
	private void reenlist(Movie m) {
		for (Media md : m.getMediaList()) {
			reenlist(md);
		}
	}
	
	@Override
	public void reenlistSubtitles(JukeboxRequestID request, StreamObserver<Empty> responseObserver) {

		this.getLog().Debug(String.format("Re-enlist subtitle -- %s", request.getId()));

		Runnable r = () -> {
			try {
				if (request.getRequestType() == RequestType.TypeMovie) {
					Movie m = this.getDatabase().getMovie(request.getId());
					this.getSubtitleDownloader().reenlistMovie(m);	
				}
				else if (request.getRequestType() == RequestType.TypeEpisode) {
					Episode ep = this.getDatabase().getEpisode(request.getId());
					this.getSubtitleDownloader().reenlistEpisode(ep);
				}
			}
			catch (Exception e) {
				Logger.log("Error occured in reenlistSubtitles", e);
			}
		};
		this.getExecutor().start(r);

		responseObserver.onNext(Empty.newBuilder().build());
		responseObserver.onCompleted();
	}
	
	private void reenlist(Media md) {
		File file = new File(this.getUtils().getFullFilePath(md));
		
		// create a file representation based on the values of the media object
		FileRepresentation f = new FileRepresentation(md.getFilepath(), md.getFilename(), this.getUtils().getCurrentTimestamp(), file.length());
		
		// re-enlist the file into the movie identifier
		this.getMovieIdentifier().addFile(f);
	}

	private void reenlist(Series s) {
		for (Season sn : s.getSeasonList()) {
			reenlist(sn);
		}	
	}
	
	private void reenlist(Season sn) {
		for (Episode ep: sn.getEpisodeList()) {
			reenlist(ep);
		}	
	}
	
	private void reenlist(Episode ep) {
		for (Media md : ep.getMediaList()) {
			reenlist(md);
		}	
	}
	
	private void setPriority() {
		Thread.currentThread().setPriority(7);
	}

	@Override
	public void getItem(JukeboxRequestGetItem request, StreamObserver<JukeboxResponseGetItem> responseObserver) {
		setPriority();
		this.getLog().Debug(String.format("GetItem :: %s - %s", request.getID(), request.getRequestType()));
		
		JukeboxResponseGetItem.Builder b = JukeboxResponseGetItem.newBuilder();
		boolean excludeImages = request.getFilterList().stream().anyMatch(r -> r == RequestFilter.Images);
		boolean excludeTextData = request.getFilterList().stream().anyMatch(r -> r == RequestFilter.SubsTextdata);
		
		switch (request.getRequestType()) {		
		case TypeMovie:
			b.setMovie(
				this.getDatabase().searchMoviesByID(
					request.getID(),
					excludeImages,
					excludeTextData));
			break;
		case TypeSeries:
			b.setSerie(
				this.getDatabase().searchSeriesById(
					request.getID(),
					excludeImages));
			break;
		case TypeSeason:
			b.setSeason(
				this.getDatabase().searchSeasonById(
					request.getID(),
					excludeImages,
					excludeTextData));
			break;
		case TypeEpisode:
			b.setEpisode(
				this.getDatabase().searchEpisodeById(
					request.getID(),
					excludeImages,
					excludeTextData));
						
		default:
			break;
		}

		responseObserver.onNext(b.build());
		responseObserver.onCompleted();
	}

	@Override
	public void reenlistMetadata(JukeboxRequestID request, StreamObserver<Empty> responseObserver) {
		Runnable r = () -> {
			try {
				if (request.getRequestType() == RequestType.TypeMovie) {
					Movie m = this.getDatabase().getMovie(request.getId());
					this.getMovieIdentifier().getMovieInfo(m, m.getMedia(0));
					this.getDatabase().save(m);	
				}
				else if (request.getRequestType() == RequestType.TypeEpisode) {
					Episode ep = this.getDatabase().getEpisode(request.getId());
					Series s = this.getDatabase().getSeriesByEpisode(request.getId());
					if (s != null) {
						this.getLog().Debug("Series found. Finding season...");
						Season sn = DomainUtil.findSeasonByEpisodeId(s, request.getId());
						
						updateEpisodeInfo(s, sn, ep);
					}
					else {
						this.getLog().Debug("Series not found. exiting...");
					}
				}
			}
			catch (Exception e) {
				Logger.log("Error occured in reenlistSubtitles", e);
			}
		};
		this.getExecutor().start(r);

		responseObserver.onNext(Empty.newBuilder().build());
		responseObserver.onCompleted();
	}

	private void updateEpisodeInfo(Series s, Season sn, Episode ep) {
		if (sn != null) {
			int season = sn.getSeasonNumber();
			
			this.getLog().Debug("Season found. Updating info on episode...");
			
			s = this.getMovieIdentifier().getSeriesInfo(
					s, 
					season, 
					ep.getEpisodeNumber(), 
					ep.getMedia(0));
			
			Episode newEpisode = DomainUtil.findEpisode(s, season, ep.getEpisodeNumber());
			this.getDatabase().save(newEpisode);
		}
		else {
			this.getLog().Debug("Season not found -- exiting");
		}
	}

	@Override
	public void forceConverter(JukeboxRequestID request, StreamObserver<Empty> responseObserver) {
		this.getDatabase().forceConversion(request.getId());
		responseObserver.onNext(Empty.newBuilder().build());
		responseObserver.onCompleted();
	}
}
