package se.qxx.jukebox.servercomm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.MovieIdentifier;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Episode.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestID;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
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
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Players.Server;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;
import se.qxx.jukebox.watcher.FileRepresentation;
import se.qxx.jukebox.webserver.StreamingWebServer;
import se.qxx.jukebox.vlc.Distributor;

public class JukeboxRpcServerConnection extends JukeboxService {

	@Override 
	public void listMovies(RpcController controller,
			JukeboxRequestListMovies request,
			RpcCallback<JukeboxResponseListMovies> done) {

		Log.Debug("ListMovies", LogType.COMM);
		
		String searchString = request.getSearchString();
		List<Movie> list = DB.searchMoviesByTitle(searchString, true, true);
		List<Series> listSeries = DB.searchSeriesByTitle(searchString, true, true);
		
		if (!request.getReturnFullSizePictures()) {
			list = removeFullSizePicturesAndSubsFromMovies(list);
			listSeries = removeFullSizePicturesAndSubsFromSeries(listSeries);
		}
		
		JukeboxResponseListMovies lm = JukeboxResponseListMovies.newBuilder()
				.addAllMovies(list)
				.addAllSeries(listSeries)
				.build();
		done.run(lm);
	}

	private List<Series> removeFullSizePicturesAndSubsFromSeries(List<Series> listSeries) {
		List<Series> listSeriesNoPics = new ArrayList<Series>();

		for (Series s : listSeries) {
			for (Season sn : s.getSeasonList()) {
				for (Episode e : sn.getEpisodeList()) {
 					
 					List<Media> newMediaList = removeSubs(e.getMediaList());
 					
					Episode e1 = Episode.newBuilder(e)
							.setImage(ByteString.EMPTY)
							.clearMedia()
							.addAllMedia(newMediaList)
							.build();
					
					DomainUtil.updateEpisode(sn, e1);
				}
				
				Season sn1 = Season.newBuilder(sn).setImage(ByteString.EMPTY).build();
				DomainUtil.updateSeason(s, sn1);
			}
			
			Series s1 = Series.newBuilder(s).setImage(ByteString.EMPTY).build();
			listSeriesNoPics.add(s1);
		}
		
		return listSeriesNoPics;
	}

	private List<Media> removeSubs(List<Media> mediaList) {
		List<Media> newMediaList = new ArrayList<Media>();
		
		for (Media md : mediaList) {
			newMediaList.add(removeSubs(md));
		}
	
		return newMediaList;
	}

	private Media removeSubs(Media md) {
		return Media.newBuilder(md).clearSubs().build();
	}

	private List<Movie> removeFullSizePicturesAndSubsFromMovies(List<Movie> listMovies) {
		List<Movie> listNoPics = new ArrayList<Movie>();
		for (Movie m : listMovies) {
			List<Media> newMediaList = removeSubs(m.getMediaList());
			
			listNoPics.add(
					Movie.newBuilder(m)
					.setImage(ByteString.EMPTY)
					.clearMedia()
					.addAllMedia(newMediaList)
					.build());
		}
		
		return listNoPics;
	}
 
	@Override
	public void listPlayers(RpcController controller, Empty request,
			RpcCallback<JukeboxResponseListPlayers> done) {

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

		Log.Debug("StartMovie", LogType.COMM);
		Log.Debug(String.format("Starting %s with ID: %s on player %s", request.getRequestType(), request.getMovieOrEpisodeId(), request.getPlayerName()), Log.LogType.COMM);
		
		try {
			Media md = getMedia(request);

			boolean success = false;
			
			String uri = StringUtils.EMPTY;
			List<String> subtitleUris = new ArrayList<String>();
			
			if (StringUtils.equalsIgnoreCase("Chromecast", request.getPlayerName())) {
				//this is a chromecast request. Serve the file using http and return the uri.
				//also serve the subtitles and return them
			
				uri = serveChromecast(md, subtitleUris);
				success = true;
			}
			else {
				success = Distributor.get().startMovie(request.getPlayerName(), md);
			}
			
			if (success) {
				List<Subtitle> subs = md.getSubsList();
				
				JukeboxResponseStartMovie ls = JukeboxResponseStartMovie.newBuilder()
						.addAllSubtitle(subs)
						.setUri(uri)
						.addAllSubtitleUris(subtitleUris)
						.build();
									
				done.run(ls);
				
			}
			else
				controller.setFailed("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			controller.setFailed("Error occured when connecting to target media player"); 
		}		
		
	}

	private Media getMedia(JukeboxRequestStartMovie request) {
		Media md = null;
		if (request.getRequestType() == RequestType.TypeMovie) {
			return DB.getMovie(request.getMovieOrEpisodeId()).getMedia(0);
		}
		else if (request.getRequestType() == RequestType.TypeEpisode) {
			return DB.getEpisode(request.getMovieOrEpisodeId()).getMedia(0);
		}
		
		return null;
	}

	private String serveChromecast(Media md, List<String> subtitleUris) {
		String uri;

		uri = StreamingWebServer.get().registerFile(String.format("%s/%s", md.getFilepath(), md.getFilename()));
		
		Log.Debug(String.format("Number of subtitles :: %s", md.getSubsCount()), LogType.COMM);
		for (Subtitle s : md.getSubsList()) {
			String subFilename = StreamingWebServer.get().registerSubtitle(s);
			subtitleUris.add(subFilename);
		}
		return uri;
	}

	@Override
	public void stopMovie(RpcController controller,
			JukeboxRequestGeneral request, RpcCallback<Empty> done) {

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
		
		Log.Debug("GetTitle -- EMPTY", LogType.COMM);
	}

	@Override
	public void blacklist(RpcController controller,
			JukeboxRequestID request, RpcCallback<Empty> done) {
		
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
		
		Log.Debug("ToggleWatched -- EMPTY", LogType.COMM);
	}

	@Override
	public void listSubtitles(RpcController controller,
			JukeboxRequestListSubtitles request,
			RpcCallback<JukeboxResponseListSubtitles> done) {

		Log.Debug(String.format("Getting list of subtitles for media ID :: %s", request.getMediaId()), Log.LogType.COMM);

		
		Media media = DB.getMediaById(request.getMediaId());		
	
		JukeboxResponseListSubtitles ls = JukeboxResponseListSubtitles.newBuilder()
				.addAllSubtitle(media.getSubsList())
				.build();
					
		done.run(ls);
	}

	@Override
	public void setSubtitle(RpcController controller,
			JukeboxRequestSetSubtitle request, RpcCallback<Empty> done) {

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
		
		Log.Debug("Re-identify -- EMPTY", LogType.COMM);

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
			
			done.run(Empty.newBuilder().build());
		}
		catch (Exception e) {
			controller.setFailed("Error occured when deleting object from database");
		}
	}
	
	private void reenlist(Movie m) {
		for (Media md : m.getMediaList()) {
			reenlist(md);
		}
	}
	
	private void reenlist(Media md) {
		File file = new File(String.format("%s/%s", md.getFilepath(), md.getFilename()));
		
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
	
}
