package se.qxx.jukebox.servercomm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.MovieIdentifier;
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
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Players.Server;
import se.qxx.jukebox.subtitles.SubtitleDownloader;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.webserver.StreamingFile;
import se.qxx.jukebox.webserver.StreamingWebServer;
import se.qxx.protodb.Logger;
import se.qxx.jukebox.vlc.Distributor;

public class JukeboxRpcServerConnection extends JukeboxService {
	private IDatabase database;
	private ISettings settings;
	private IDistributor distributor;
	private IStreamingWebServer webServer;
	private ISubtitleDownloader subtitleDownloader;
	private IMovieIdentifier movieIdentifier;
	
	@Inject
	public JukeboxRpcServerConnection(
			ISettings settings, 
			IDatabase database, 
			IDistributor distributor,
			IStreamingWebServer webServer,
			ISubtitleDownloader subtitleDownloader,
			IMovieIdentifier movieIdentifier) {
		super();
		this.setDatabase(database);
		this.setSettings(settings);
		this.setDistributor(distributor);
		this.setWebServer(webServer);
		this.setSubtitleDownloader(subtitleDownloader);
		this.setMovieIdentifier(movieIdentifier);
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
	@Override 
	public void listMovies(RpcController controller,
			JukeboxRequestListMovies request,
			RpcCallback<JukeboxResponseListMovies> done) {

		setPriority();
		Log.Debug(String.format("ListMovies :: %s", request.getRequestType()), LogType.COMM);
		
		String searchString = request.getSearchString();
		
		JukeboxResponseListMovies.Builder b = JukeboxResponseListMovies.newBuilder();
		
		switch (request.getRequestType()) {
		case TypeMovie:
			b.addAllMovies(
				this.getDatabase().searchMoviesByTitle(
						searchString, 
						request.getNrOfItems(), 
						request.getStartIndex()));
			
			b.setTotalMovies(
				this.getDatabase().getTotalNrOfMovies());
			
			break;
		case TypeSeries:
			b.addAllSeries(
				this.getDatabase().searchSeriesByTitle(
						searchString, 
						request.getNrOfItems(), 
						request.getStartIndex()));
			
			b.setTotalSeries(
				this.getDatabase().getTotalNrOfSeries());
			
			break;
		default:
			break;
		}
		
		JukeboxResponseListMovies response = b.build();
		logResponse(response);
		
		done.run(
			b.build());
	}

	private void logResponse(JukeboxResponseListMovies response) {
		Log.Debug(String.format("Movie count :: %s ", response.getMoviesCount()), LogType.COMM);
		Log.Debug(String.format("Series count :: %s ", response.getSeriesCount()), LogType.COMM);
		logSeasons(response);
		logEpisodes(response);
	}

	private void logSeasons(JukeboxResponseListMovies response) {
		int c = 0;
		for (Series s : response.getSeriesList())
			c += s.getSeasonCount();
		
		Log.Debug(String.format("Season count :: %s ", c), LogType.COMM);
	}
	
	private void logEpisodes (JukeboxResponseListMovies response) {
		int c = 0;
		for (Series s : response.getSeriesList()) {
			for (Season ss : s.getSeasonList()) {
				c += ss.getEpisodeCount();
			}
		}
		
		Log.Debug(String.format("Episode count :: %s ", c), LogType.COMM);
	}

	@Override
	public void listPlayers(RpcController controller, Empty request,
			RpcCallback<JukeboxResponseListPlayers> done) {

		setPriority();
		Log.Debug("ListPlayers", LogType.COMM);

		Collection<String> hostnames = new ArrayList<String>();
		for (Server s : this.getSettings().getSettings().getPlayers().getServer()) {
			hostnames.add(s.getName());
		}
		
		JukeboxResponseListPlayers lp = JukeboxResponseListPlayers.newBuilder().addAllHostname(hostnames).build();

		done.run(lp);
	}

	@Override
	public void startMovie(RpcController controller,
			JukeboxRequestStartMovie request,
			RpcCallback<JukeboxResponseStartMovie> done) {

		setPriority();
		Log.Debug("StartMovie", LogType.COMM);
		Log.Debug(String.format("Starting %s with ID: %s on player %s", request.getRequestType(), request.getMovieOrEpisodeId(), request.getPlayerName()), Log.LogType.COMM);
		
		try {
			Media md = getMedia(request);

			boolean success = false;

			StreamingFile streamingFile = null;
			List<String> subtitleUris = new ArrayList<String>();
			
			if (StringUtils.equalsIgnoreCase("Chromecast", request.getPlayerName())) {
				//this is a chromecast request. Serve the file using http and return the uri.
				//also serve the subtitles and return them
			
				streamingFile = serveChromecast(md, subtitleUris);
				success = true;
			}
			else {
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
									
				done.run(
					b.build());
				
			}
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			Log.Error("Error occured when starting movie", LogType.COMM, e);
			controller.setFailed("Error occured when connecting to target media player"); 
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

	private StreamingFile serveChromecast(Media md, List<String> subtitleUris) {
		// if media contains subtitles (i.e. mkv) then extract the file and put it into a file for serving
		// https://github.com/matthewn4444/EBMLReader ??
		StreamingFile streamingFile = this.getWebServer().registerFile(md);
		
		Log.Debug(String.format("Number of subtitles :: %s", md.getSubsCount()), LogType.COMM);
		for (Subtitle s : md.getSubsList()) {
			StreamingFile subFile = this.getWebServer().registerSubtitle(s);
			
			if (subFile != null)
				subtitleUris.add(subFile.getUri());
		}
		
		return streamingFile;
	}

	@Override
	public void stopMovie(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<Empty> done) {

		setPriority();
		Log.Debug("StopMovie", LogType.COMM);
		Log.Debug(String.format("Stopping movie on player %s", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().stopMovie(request.getPlayerName()))
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
			
		}		

	}

	@Override
	public void pauseMovie(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<Empty> done) {

		setPriority();
		Log.Debug("PauseMovie", LogType.COMM);
		Log.Debug(String.format("Pausing movie on player %s", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().pauseMovie(request.getPlayerName()))
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}		
		
	}

	@Override
	public void seek(RpcController controller, JukeboxRequestSeek request,
			RpcCallback<Empty> done) {

		setPriority();
		Log.Debug(String.format("Seeking on player %s to %s seconds", request.getPlayerName(), request.getSeconds()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().seek(request.getPlayerName(), request.getSeconds()))
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}		
	}

	@Override
	public void switchVRatio(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<Empty> done) {

		setPriority();
		Log.Debug("SwitchVRatio", LogType.COMM);
		Log.Debug(String.format("Toggling vratio on %s...", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().toggleVRatio(request.getPlayerName()))
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}		

	}

	@Override
	public void getTime(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<JukeboxResponseTime> done) {

		setPriority();
		Log.Debug("GetTime", LogType.COMM);
		Log.Debug(String.format("Getting time on %s...", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			String response = this.getDistributor().getTime(request.getPlayerName());
			if (response.equals(StringUtils.EMPTY))
				controller.setFailed("Error occured when connecting to target media player"); 
			else {
				int seconds = Integer.parseInt(response);
				String titleFilename = getTitleFilename(request.getPlayerName());
				
				JukeboxResponseTime time = JukeboxResponseTime.newBuilder()
						.setSeconds(seconds)
						.setFilename(titleFilename)
						.build();

				done.run(time);
			}
				
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
			
		}		
		
	}

	@Override
	public void isPlaying(RpcController controller,
			JukeboxRequestGeneral request,
			RpcCallback<JukeboxResponseIsPlaying> done) {

		setPriority();
		Log.Debug("IsPlaying", LogType.COMM);
		Log.Debug(String.format("Getting is playing status on %s...", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			boolean isPlaying = this.getDistributor().isPlaying(request.getPlayerName());

			JukeboxResponseIsPlaying r = JukeboxResponseIsPlaying.newBuilder().setIsPlaying(isPlaying).build();	
			done.run(r);				
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}				
	}

	@Override
	public void getTitle(RpcController controller,
			JukeboxRequestGeneral request,
			RpcCallback<JukeboxResponseGetTitle> done) {
		// TODO Auto-generated method stub
		
		setPriority();
		Log.Debug("GetTitle -- EMPTY", LogType.COMM);
	}

	@Override
	public void blacklist(RpcController controller,
			JukeboxRequestID request, RpcCallback<Empty> done) {
		
		setPriority();
		Log.Debug("Blacklist -- EMPTY", LogType.COMM);

		if (request.getRequestType() == RequestType.TypeMovie) {
			Movie m = this.getDatabase().getMovie(request.getId());
			if (m != null)
				this.getDatabase().addToBlacklist(m);
		}
		
		if (request.getRequestType() == RequestType.TypeEpisode) {
			Log.Debug("Not Implemented yet -- Blacklist of episodes", LogType.COMM);
		}
		
		done.run(Empty.newBuilder().build());
	}

	@Override
	public void toggleWatched(RpcController controller,
			JukeboxRequestID request, RpcCallback<Empty> done) {
		// TODO Auto-generated method stub
		
		setPriority();
		Log.Debug("ToggleWatched -- EMPTY", LogType.COMM);
	}

	@Override
	public void listSubtitles(RpcController controller,
			JukeboxRequestListSubtitles request,
			RpcCallback<JukeboxResponseListSubtitles> done) {

		setPriority();
		Log.Debug(String.format("Getting list of subtitles for media ID :: %s", request.getMediaId()), Log.LogType.COMM);

		
		Media media = this.getDatabase().getMediaById(request.getMediaId());

		JukeboxResponseListSubtitles.Builder b = JukeboxResponseListSubtitles.newBuilder();
		b.addAllSubtitle(media.getSubsList());
	
		done.run(b.build());
	}

	@Override
	public void setSubtitle(RpcController controller,
			JukeboxRequestSetSubtitle request, RpcCallback<Empty> done) {
		
		setPriority();
		Log.Debug(String.format("Setting subtitle on %s...", request.getPlayerName()), Log.LogType.COMM);
		
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
						true))
					done.run(Empty.newBuilder().build());
				else
					controller.setFailed("Error occured when connecting to target media player"); 
			}
			else {
				controller.setFailed("Subtitle track with that description was not found");
			}
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}		
		
	}

	@Override
	public void wakeup(RpcController controller, JukeboxRequestGeneral request,
			RpcCallback<Empty> done) {

		setPriority();
		Log.Debug(String.format("Waking up player %s", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().wakeup(request.getPlayerName()))		
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Could not wake up computer. Error while connecting.");
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}
				
		
	}

	@Override
	public void suspend(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<Empty> done) {

		setPriority();
		Log.Debug(String.format("Suspending computer with player %s...", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().suspend(request.getPlayerName()))
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Error occured when connecting to target control service"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target control service"); 
			
		}				
		
	}

	@Override
	public void toggleFullscreen(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<Empty> done) {

		setPriority();
		Log.Debug(String.format("Toggling fullscreen...", request.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (this.getDistributor().toggleFullscreen(request.getPlayerName()))
				done.run(Empty.newBuilder().build());
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 	
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
	public void reIdentify(RpcController controller, JukeboxRequestID request,
			RpcCallback<Empty> done) {
		
		setPriority();
		Log.Debug("Re-identify -- EMPTY", LogType.COMM);

		Thread t = new Thread(() -> {
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
				Log.Error("Error occured when deleting object from database", LogType.COMM);
			}			
		});
		t.start();
		
		done.run(Empty.newBuilder().build());
	}
	
	private void reenlist(Movie m) {
		for (Media md : m.getMediaList()) {
			reenlist(md);
		}
	}
	
	@Override
	public void reenlistSubtitles(RpcController controller,
			JukeboxRequestID request, RpcCallback<Empty> done) {

		Log.Debug(String.format("Re-enlist subtitle -- %s", request.getId()), LogType.COMM);

		Thread t = new Thread(() -> {
			try {
				if (request.getRequestType() == RequestType.TypeMovie) {
					Movie m = this.getDatabase().getMovie(request.getId());
					this.getSubtitleDownloader().reenlistMovie(m);	
				}
				else if (request.getRequestType() == RequestType.TypeEpisode) {
					Episode ep = this.getDatabase().getEpisode(request.getId());
					this.getSubtitleDownloader().reenlistEpisode(ep);
				}
			
				done.run(Empty.newBuilder().build());
			}
			catch (Exception e) {
				Logger.log("Error occured in reenlistSubtitles", e);
				controller.setFailed("Error occured when enlisting to subtitle downloader");
			}
		});
		t.start();
		
		done.run(Empty.newBuilder().build());
	}
	
	private void reenlist(Media md) {
		File file = new File(Util.getFullFilePath(md));
		
		// create a file representation based on the values of the media object
		FileRepresentation f = new FileRepresentation(md.getFilepath(), md.getFilename(), Util.getCurrentTimestamp(), file.length());
		
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
	public void getItem(RpcController controller, JukeboxRequestGetItem request,
			RpcCallback<JukeboxResponseGetItem> done) {
		setPriority();
		Log.Debug(String.format("GetItem :: %s - %s", request.getID(), request.getRequestType()), LogType.COMM);
		
		
		JukeboxResponseGetItem.Builder b = JukeboxResponseGetItem.newBuilder();
		switch (request.getRequestType()) {		
		case TypeMovie:
			b.setMovie(
				this.getDatabase().searchMoviesByID(
					request.getID()));
			break;
		case TypeSeries:
			b.setSerie(
				this.getDatabase().searchSeriesById(
					request.getID()));
			break;
		case TypeSeason:
			b.setSeason(
				this.getDatabase().searchSeasonById(
					request.getID()));
			break;
		default:
			break;
		}
		
		done.run(b.build());
	}

	@Override
	public void reenlistMetadata(RpcController controller, JukeboxRequestID request, RpcCallback<Empty> done) {
		Thread t = new Thread(() -> {
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
						Log.Debug("Series found. Finding season...", LogType.COMM);
						Season sn = DomainUtil.findSeasonByEpisodeId(s, request.getId());
						
						updateEpisodeInfo(s, sn, ep);
					}
					else {
						Log.Debug("Series not found. exiting...", LogType.COMM);
					}
				}
			
				done.run(Empty.newBuilder().build());
			}
			catch (Exception e) {
				Logger.log("Error occured in reenlistSubtitles", e);
				controller.setFailed("Error occured when enlisting to subtitle downloader");
			}
		});
		t.start();		
	}

	private void updateEpisodeInfo(Series s, Season sn, Episode ep) {
		if (sn != null) {
			int season = sn.getSeasonNumber();
			
			Log.Debug("Season found. Updating info on episode...", LogType.COMM);
			
			s = this.getMovieIdentifier().getSeriesInfo(
					s, 
					season, 
					ep.getEpisodeNumber(), 
					ep.getMedia(0));
			
			Episode newEpisode = DomainUtil.findEpisode(s, season, ep.getEpisodeNumber());
			this.getDatabase().save(newEpisode);
		}
		else {
			Log.Debug("Season not found -- exiting", LogType.COMM);
		}
	}
	
}
