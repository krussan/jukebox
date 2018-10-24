package se.qxx.jukebox.servercomm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.MovieIdentifier;
import se.qxx.jukebox.SubtitleDownloader;
import se.qxx.jukebox.Log.LogType;
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
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Players.Server;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.webserver.StreamingFile;
import se.qxx.jukebox.webserver.StreamingWebServer;
import se.qxx.protodb.Logger;
import se.qxx.jukebox.vlc.Distributor;

public class JukeboxRpcServerConnection extends JukeboxService {

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
				DB.searchMoviesByTitle(
						searchString, 
						request.getNrOfItems(), 
						request.getStartIndex()));
			
			b.setTotalMovies(
				DB.getTotalNrOfMovies());
			
			break;
		case TypeSeries:
			b.addAllSeries(
				DB.searchSeriesByTitle(
						searchString, 
						request.getNrOfItems(), 
						request.getStartIndex()));
			
			b.setTotalSeries(
				DB.getTotalNrOfSeries());
			
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
		for (Server s : Settings.get().getPlayers().getServer()) {
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
				success = Distributor.get().startMovie(request.getPlayerName(), md);
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
			return DB.getMovie(request.getMovieOrEpisodeId()).getMedia(0);
		}
		else if (request.getRequestType() == RequestType.TypeEpisode) {
			return DB.getEpisode(request.getMovieOrEpisodeId()).getMedia(0);
		}
		
		return null;
	}

	private StreamingFile serveChromecast(Media md, List<String> subtitleUris) {
		// if media contains subtitles (i.e. mkv) then extract the file and put it into a file for serving
		// https://github.com/matthewn4444/EBMLReader ??
		StreamingFile streamingFile = StreamingWebServer.get().registerFile(md);
		
		Log.Debug(String.format("Number of subtitles :: %s", md.getSubsCount()), LogType.COMM);
		for (Subtitle s : md.getSubsList()) {
			StreamingFile subFile = StreamingWebServer.get().registerSubtitle(s);
			
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
			if (Distributor.get().stopMovie(request.getPlayerName()))
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
			if (Distributor.get().pauseMovie(request.getPlayerName()))
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
			if (Distributor.get().seek(request.getPlayerName(), request.getSeconds()))
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
			if (Distributor.get().toggleVRatio(request.getPlayerName()))
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
			String response = Distributor.get().getTime(request.getPlayerName());
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
			boolean isPlaying = Distributor.get().isPlaying(request.getPlayerName());

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
			Movie m = DB.getMovie(request.getId());
			if (m != null)
				DB.addToBlacklist(m);
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

		
		Media media = DB.getMediaById(request.getMediaId());

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
		Movie m = DB.getMovieByMediaID(mediaID);
		
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
				if (Distributor.get().restartWithSubtitle(
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
			if (Distributor.get().wakeup(request.getPlayerName()))		
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
			if (Distributor.get().suspend(request.getPlayerName()))
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
			if (Distributor.get().toggleFullscreen(request.getPlayerName()))
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
			return StringUtils.trim(Distributor.get().getTitle(playerName));
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
					Movie m = DB.getMovie(request.getId());
					DB.delete(m);
					reenlist(m);
				}
				
				if (request.getRequestType() == RequestType.TypeSeries) {
					Series s = DB.getSeries(request.getId());
					DB.delete(s);			
					reenlist(s);			
				}

				if (request.getRequestType() == RequestType.TypeSeason) {
					Season sn = DB.getSeason(request.getId());
					DB.delete(sn);			
					reenlist(sn);			
				}

				if (request.getRequestType() == RequestType.TypeEpisode) {
					Episode ep = DB.getEpisode(request.getId());
					DB.delete(ep);			
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

		try {
			if (request.getRequestType() == RequestType.TypeMovie) {
				Movie m = DB.getMovie(request.getId());
				SubtitleDownloader.get().reenlistMovie(m);	
			}
			else if (request.getRequestType() == RequestType.TypeEpisode) {
				Episode ep = DB.getEpisode(request.getId());
				SubtitleDownloader.get().reenlistEpisode(ep);
			}
		
			done.run(Empty.newBuilder().build());
		}
		catch (Exception e) {
			Logger.log("Error occured in reenlistSubtitles", e);
			controller.setFailed("Error occured when enlisting to subtitle downloader");
		}
	}
	
	private void reenlist(Media md) {
		File file = new File(Util.getFullFilePath(md));
		
		// create a file representation based on the values of the media object
		FileRepresentation f = new FileRepresentation(md.getFilepath(), md.getFilename(), Util.getCurrentTimestamp(), file.length());
		
		// re-enlist the file into the movie identifier
		MovieIdentifier.get().addFile(f);
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
				DB.searchMoviesByID(
					request.getID()));
			break;
		case TypeSeries:
			b.setSerie(
				DB.searchSeriesById(
					request.getID()));
			break;
		case TypeSeason:
			b.setSeason(
				DB.searchSeasonById(
					request.getID()));
			break;
		default:
			break;
		}
		
		done.run(b.build());
	}
	
}
