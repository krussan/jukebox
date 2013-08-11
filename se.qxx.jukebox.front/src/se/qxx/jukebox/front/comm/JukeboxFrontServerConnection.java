package se.qxx.jukebox.front.comm;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxFrontSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxFrontService;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxFrontStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;
import se.qxx.jukebox.front.MovieStatusListener;

public class JukeboxFrontServerConnection extends JukeboxFrontService {

    private MovieStatusListener listener;

	public JukeboxFrontServerConnection() {
		
	}
//	@Override
//	public void listMovies(RpcController controller,
//			JukeboxRequestListMovies request,
//			RpcCallback<JukeboxResponseListMovies> done) {
//
//	}
//
//	@Override
//	public void listPlayers(RpcController controller, Empty request,
//			RpcCallback<JukeboxResponseListPlayers> done) {
//
//	}
//
//	@Override
//	public void startMovie(RpcController controller,
//			JukeboxRequestStartMovie request,
//			RpcCallback<JukeboxResponseStartMovie> done) {
//
//
//	}
//
//	@Override
//	public void stopMovie(RpcController controller,
//			JukeboxRequestGeneral request, RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Stopping movie on player %s", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().stopMovie(request.getPlayerName()))
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Error occured when connecting to target media player"); 
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//			
//		}		
//
//	}
//
//	@Override
//	public void pauseMovie(RpcController controller,
//			JukeboxRequestGeneral request, RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Pausing movie on player %s", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().pauseMovie(request.getPlayerName()))
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Error occured when connecting to target media player"); 
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//		}		
//		
//	}
//
//	@Override
//	public void seek(RpcController controller, JukeboxRequestSeek request,
//			RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Seeking on player %s to %s seconds", request.getPlayerName(), request.getSeconds()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().seek(request.getPlayerName(), request.getSeconds()))
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Error occured when connecting to target media player"); 
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//		}		
//	}
//
//	@Override
//	public void switchVRatio(RpcController controller,
//			JukeboxRequestGeneral request, RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Toggling vratio on %s...", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().toggleVRatio(request.getPlayerName()))
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Error occured when connecting to target media player"); 
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//		}		
//
//	}
//
//	@Override
//	public void getTime(RpcController controller,
//			JukeboxRequestGeneral request, RpcCallback<JukeboxResponseTime> done) {
//
//		Log.Debug(String.format("Getting time on %s...", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			String response = VLCDistributor.get().getTime(request.getPlayerName());
//			if (response.equals(StringUtils.EMPTY))
//				controller.setFailed("Error occured when connecting to target media player"); 
//			else {
//				int seconds = Integer.parseInt(response);
//				String titleFilename = getTitleFilename(request.getPlayerName());
//				
//				JukeboxResponseTime time = JukeboxResponseTime.newBuilder()
//						.setSeconds(seconds)
//						.setFilename(titleFilename)
//						.build();
//
//				done.run(time);
//			}
//				
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//			
//		}		
//		
//	}
//
//	@Override
//	public void isPlaying(RpcController controller,
//			JukeboxRequestGeneral request,
//			RpcCallback<JukeboxResponseIsPlaying> done) {
//
//		Log.Debug(String.format("Getting is playing status on %s...", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			boolean isPlaying = VLCDistributor.get().isPlaying(request.getPlayerName());
//
//			JukeboxResponseIsPlaying r = JukeboxResponseIsPlaying.newBuilder().setIsPlaying(isPlaying).build();	
//			done.run(r);				
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//		}				
//	}
//
//	@Override
//	public void getTitle(RpcController controller,
//			JukeboxRequestGeneral request,
//			RpcCallback<JukeboxResponseGetTitle> done) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void blacklist(RpcController controller,
//			JukeboxRequestMovieID request, RpcCallback<Empty> done) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void toggleWatched(RpcController controller,
//			JukeboxRequestMovieID request, RpcCallback<Empty> done) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void listSubtitles(RpcController controller,
//			JukeboxRequestListSubtitles request,
//			RpcCallback<JukeboxResponseListSubtitles> done) {
//
//		Log.Debug(String.format("Getting list of subtitles for media ID :: %s", request.getMediaId()), Log.LogType.COMM);
//
//		Media media = DB.getMediaById(request.getMediaId());		
//		List<Subtitle> subs = DB.getSubtitles(media);
//	
//		JukeboxResponseListSubtitles ls = JukeboxResponseListSubtitles.newBuilder()
//				.addAllSubtitle(subs)
//				.build();
//					
//		done.run(ls);
//	}
//
//	@Override
//	public void setSubtitle(RpcController controller,
//			JukeboxRequestSetSubtitle request, RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Setting subtitle on %s...", request.getPlayerName()), Log.LogType.COMM);
//		
//		Media md = DB.getMediaById(request.getMediaID());
//		Movie m = DB.getMovieByStartOfMediaFilename(md.getFilename());		
//		List<Subtitle> subs = DB.getSubtitles(md);
//		
//		
//		// It appears that VLC RC interface only reads the first sub-file option specified
//		// in the command sent. Thus we need to clear playlist and restart video each time we
//		// change the subtitle track.
//		Subtitle subTrack = getSubtitleTrack(request.getSubtitleDescription(), subs);
//		
//		//VLCDistributor.restart()
//		
//		try {
//			if (subTrack != null) {
//				if (VLCDistributor.get().restartWithSubtitle(
//						request.getPlayerName(), 
//						m, 
//						subTrack.getFilename(), 
//						true))
//					done.run(Empty.newBuilder().build());
//				else
//					controller.setFailed("Error occured when connecting to target media player"); 
//			}
//			else {
//				controller.setFailed("Subtitle track with that description was not found");
//			}
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//			
//		}		
//		
//	}
//
//	@Override
//	public void wakeup(RpcController controller, JukeboxRequestGeneral request,
//			RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Waking up player %s", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().wakeup(request.getPlayerName()))		
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Could not wake up computer. Error while connecting.");
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 
//		}
//				
//		
//	}
//
//	@Override
//	public void suspend(RpcController controller,
//			JukeboxRequestGeneral request, RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Suspending computer with player %s...", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().suspend(request.getPlayerName()))
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Error occured when connecting to target control service"); 
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target control service"); 
//			
//		}				
//		
//	}
//
//	@Override
//	public void toggleFullscreen(RpcController controller,
//			JukeboxRequestGeneral request, RpcCallback<Empty> done) {
//
//		Log.Debug(String.format("Toggling fullscreen...", request.getPlayerName()), Log.LogType.COMM);
//		
//		try {
//			if (VLCDistributor.get().toggleFullscreen(request.getPlayerName()))
//				done.run(Empty.newBuilder().build());
//			else
//				controller.setFailed("Error occured when connecting to target media player"); 
//		} catch (VLCConnectionNotFoundException e) {
//			controller.setFailed("Error occured when connecting to target media player"); 	
//		}		
//		
//	}
//
//	
//	protected Subtitle getSubtitleTrack(String description, List<Subtitle> subs) {
//		for (int i=0;i<subs.size();i++) {
//			String subDescription = subs.get(i).getDescription();
//			if (StringUtils.equalsIgnoreCase(subDescription, description)) {
//				return subs.get(i);
//			}
//		}
//		return null;
//	}	
//	
//	private String getTitleFilename(String playerName) {
//		try {
//			return StringUtils.trim(VLCDistributor.get().getTitle(playerName));
//		} catch (VLCConnectionNotFoundException e) {
//			return StringUtils.EMPTY; 			
//		}		
//	}

	@Override
	public void startMovie(RpcController controller,
			JukeboxFrontStartMovie request, RpcCallback<Empty> done) {

		try {
			this.listener.play(request.getMrl());
			
			done.run(Empty.newBuilder().build());
				
		} catch (Exception e) {
			controller.setFailed("Error occured when starting movie"); 
		}		
		
		
	}

	@Override
	public void stopMovie(RpcController controller, Empty request,
			RpcCallback<Empty> done) {
		try {
			this.listener.stop();
			
			done.run(Empty.newBuilder().build());
				
		} catch (Exception e) {
			controller.setFailed("Error occured when stopping movie"); 
		}	
	}

	@Override
	public void pauseMovie(RpcController controller, Empty request,
			RpcCallback<Empty> done) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seek(RpcController controller, JukeboxFrontSeek request,
			RpcCallback<Empty> done) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchVRatio(RpcController controller, Empty request,
			RpcCallback<Empty> done) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getTime(RpcController controller, Empty request,
			RpcCallback<JukeboxResponseTime> done) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void isPlaying(RpcController controller, Empty request,
			RpcCallback<JukeboxResponseIsPlaying> done) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getTitle(RpcController controller, Empty request,
			RpcCallback<JukeboxResponseGetTitle> done) {
		// TODO Auto-generated method stub
		
	}	
}
