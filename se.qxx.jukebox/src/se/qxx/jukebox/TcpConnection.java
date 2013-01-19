package se.qxx.jukebox; 

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestPauseMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSetSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStopMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSuspend;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestTime;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestToggleFullscreen;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestVRatio;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestWakeup;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseError;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestBlacklistMovie;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Vlc.Server;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;
import se.qxx.jukebox.vlc.VLCDistributor;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

public class TcpConnection implements Runnable {
	private Socket _client;
	
	public TcpConnection(Socket client) {
		this._client = client;
	}
	
	@Override
	public void run() {
		try {
			Log.Debug(String.format("Connection made from %s", this._client.getInetAddress().toString()), Log.LogType.COMM);
			// read 4 bytes parsing the message length
			InputStream is = this._client.getInputStream();
			DataInputStream ds = new DataInputStream(is);
			int lengthOfMessage = ds.readInt();
			byte[] data = new byte[lengthOfMessage];

			int offset = 0;
			int numRead = 0;
			while (offset < lengthOfMessage && (numRead = is.read(data, offset, lengthOfMessage - offset)) >= 0) {
				offset += numRead;
			}
			
			CodedInputStream cis = CodedInputStream.newInstance(data);
			JukeboxRequest req = JukeboxRequest.parseFrom(cis);
			
			Log.Debug(String.format("Request was of type :: %s", req.getType().toString()), Log.LogType.COMM);
			JukeboxResponse resp = handleRequest(req);
						
			if (resp != null) {
				OutputStream os = this._client.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeInt(resp.getSerializedSize());
				
				resp.writeTo(os);
			}

		} catch (IOException e) {
			Log.Error("Error while reading request from client", Log.LogType.COMM, e);
		}
	}

	private JukeboxResponse handleRequest(JukeboxRequest req) throws InvalidProtocolBufferException {
		try {
			switch (req.getType()) {			
			case ListMovies:
				return listMovies(req);
			case StartMovie:
				return startMovie(req);
			case ListPlayers:
				return listPlayers();
			case StopMovie:
				return stopMovie(req);
			case PauseMovie:
				return pauseMovie(req);
			case Wakeup:
				return wakeup(req);
			case ToggleFullscreen:
				return toggleFullscreen(req);
			case Suspend:
				return suspend(req);
			case Seek:
				return seek(req);
			case ListSubtitles:
				return listSubtitles(req);
			case VRatio:
				return toggleVRatio(req);
			case SetSubtitle:
				return setSubtitle(req);
			case Time:
				return getTime(req);
			case IsPlaying:
				return isPlaying(req);
			case GetTitle:
				return getTitle(req);
			case BlacklistMovie:
				return blacklist(req);
			default:
				break;
			}
		} catch (Exception e) {
			
			Log.Error("Error while sending response", Log.LogType.COMM, e);
		}
		
		return null;
	}

	private JukeboxResponse listMovies(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestListMovies args = JukeboxRequestListMovies.parseFrom(data);
		
		String searchString = args.getSearchString();
		
		List<Movie> list = DB.searchMoviesByTitle(searchString);

		JukeboxResponseListMovies lm = JukeboxResponseListMovies.newBuilder().addAllMovies(list).build();
    	
		JukeboxResponse resp = JukeboxResponse.newBuilder()
				.setType(JukeboxRequestType.ListMovies)
				.setArguments(lm.toByteString())
				.build();
				
		return resp;
		
	}
	
	private JukeboxResponse startMovie(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestStartMovie args = JukeboxRequestStartMovie.parseFrom(data);

		Log.Debug(String.format("Starting movie with ID: %s on player %s", args.getMovieId(), args.getPlayerName()), Log.LogType.COMM);
		
		try {
			Movie m = DB.getMovie(args.getMovieId());
			if (VLCDistributor.get().startMovie(args.getPlayerName(), m)) {
				List<Subtitle> subs = m.getMedia(0).getSubsList();
				
				JukeboxResponseStartMovie ls = JukeboxResponseStartMovie.newBuilder()
						.addAllSubtitle(subs)
						.build();
				
				JukeboxResponse resp = JukeboxResponse.newBuilder()
						.setType(JukeboxRequestType.StartMovie)
						.setArguments(ls.toByteString())
						.build();
					
				return resp;
				
			}
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
	}
	
	private JukeboxResponse stopMovie(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestStopMovie args = JukeboxRequestStopMovie.parseFrom(data);

		Log.Debug(String.format("Stopping movie on player %s", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().stopMovie(args.getPlayerName()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
	}

	private JukeboxResponse pauseMovie(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestPauseMovie args = JukeboxRequestPauseMovie.parseFrom(data);

		Log.Debug(String.format("Pausing movie on player %s", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().pauseMovie(args.getPlayerName()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
	}

	private JukeboxResponse seek(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestSeek args = JukeboxRequestSeek.parseFrom(data);

		Log.Debug(String.format("Seeking on player %s to %s seconds", args.getPlayerName(), args.getSeconds()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().seek(args.getPlayerName(), args.getSeconds()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
		}		
	}	
	
	private JukeboxResponse listSubtitles(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestListSubtitles args = JukeboxRequestListSubtitles.parseFrom(data);

		Log.Debug(String.format("Getting list of subtitles for media ID :: %s", args.getMediaId()), Log.LogType.COMM);

		Media media = DB.getMediaById(args.getMediaId());		
		List<Subtitle> subs = DB.getSubtitles(media);
	
		JukeboxResponseListSubtitles ls = JukeboxResponseListSubtitles.newBuilder()
				.addAllSubtitle(subs)
				.build();
		
		JukeboxResponse resp = JukeboxResponse.newBuilder()
				.setType(JukeboxRequestType.ListSubtitles)
				.setArguments(ls.toByteString())
				.build();
			
		return resp;
	}	
	
	private JukeboxResponse buildErrorMessage(String errorMessage) {
		
		return JukeboxResponse.newBuilder().setType(JukeboxRequestType.Error)
				.setArguments(
						JukeboxResponseError
							.newBuilder()
							.setErrorMessage(errorMessage)
							.build()
							.toByteString())
				.build();
	}

	private JukeboxResponse listPlayers() {
		Collection<String> hostnames = new ArrayList<String>();
		for (Server s : Settings.get().getVlc().getServer()) {
			hostnames.add(s.getName());
		}
		
		JukeboxResponseListPlayers lp = JukeboxResponseListPlayers.newBuilder().addAllHostname(hostnames).build();

    	
    	return JukeboxResponse.newBuilder()
    			.setType(JukeboxRequestType.ListPlayers)
    			.setArguments(lp.toByteString())
    			.build();
	}
	
	private JukeboxResponse wakeup(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestWakeup args = JukeboxRequestWakeup.parseFrom(data);

		Log.Debug(String.format("Waking up player %s", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().wakeup(args.getPlayerName()))		
				return JukeboxResponse.newBuilder()
					.setType(JukeboxRequestType.OK)
					.build();
			else
				return buildErrorMessage("Could not wake up computer. Error while connecting.");
		} catch (VLCConnectionNotFoundException e) {
			
			return buildErrorMessage("Error occured when connecting to target media player"); 
		}
				
	}

	private JukeboxResponse toggleFullscreen(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestToggleFullscreen args = JukeboxRequestToggleFullscreen.parseFrom(data);

		Log.Debug(String.format("Toggling fullscreen...", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().toggleFullscreen(args.getPlayerName()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
	}	

	private JukeboxResponse toggleVRatio(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestVRatio args = JukeboxRequestVRatio.parseFrom(data);

		Log.Debug(String.format("Toggling vratio on %s...", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().toggleVRatio(args.getPlayerName()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
	}	

	private JukeboxResponse setSubtitle(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestSetSubtitle args = JukeboxRequestSetSubtitle.parseFrom(data);

		Log.Debug(String.format("Setting subtitle on %s...", args.getPlayerName()), Log.LogType.COMM);
		
		Media md = DB.getMediaById(args.getMediaID());
		Movie m = DB.getMovieByStartOfMediaFilename(md.getFilename());		
		List<Subtitle> subs = DB.getSubtitles(md);
		
		
		// It appears that VLC RC interface only reads the first sub-file option specified
		// in the command sent. Thus we need to clear playlist and restart video each time we
		// change the subtitle track.
		Subtitle subTrack = getSubtitleTrack(args.getSubtitleDescription(), subs);
		
		//VLCDistributor.restart()
		
		try {
			if (subTrack != null) {
				if (VLCDistributor.get().restartWithSubtitle(
						args.getPlayerName(), 
						m, 
						subTrack.getFilename(), 
						true))
					return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
				else
					return buildErrorMessage("Error occured when connecting to target media player"); 
			}
			else {
				return buildErrorMessage("Subtitle track with that description was not found");
			}
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
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

	private JukeboxResponse getTime(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestTime args = JukeboxRequestTime.parseFrom(data);

		Log.Debug(String.format("Getting time on %s...", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			String response = VLCDistributor.get().getTime(args.getPlayerName());
			if (response.equals(StringUtils.EMPTY))
				return buildErrorMessage("Error occured when connecting to target media player"); 
			else {
				int seconds = Integer.parseInt(response);
				String titleFilename = getTitleFilename(args.getPlayerName());
				
				JukeboxResponseTime time = JukeboxResponseTime.newBuilder()
						.setSeconds(seconds)
						.setFilename(titleFilename)
						.build();

				return JukeboxResponse.newBuilder()
						.setType(JukeboxRequestType.Time)
						.setArguments(time.toByteString())
						.build();
			}
				
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
	}

	private JukeboxResponse getTitle(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestGetTitle args = JukeboxRequestGetTitle.parseFrom(data);

		Log.Debug(String.format("Getting title on %s...", args.getPlayerName()), Log.LogType.COMM);
		
		String response = getTitleFilename(args.getPlayerName());
		if (StringUtils.isEmpty(response)) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
		}
		else
		{
			JukeboxResponseGetTitle r = JukeboxResponseGetTitle.newBuilder().setTitle(response).build();
				
			return JukeboxResponse.newBuilder()
					.setType(JukeboxRequestType.GetTitle)
					.setArguments(r.toByteString())
					.build();
		}	
	}

	private JukeboxResponse blacklist(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestBlacklistMovie args = JukeboxRequestBlacklistMovie.parseFrom(data);

		try {
			Movie m = DB.getMovie(args.getMovieId());
			
			Log.Debug(String.format("Blacklisting movie :: %s", m.getTitle()), LogType.COMM);
			
			DB.addToBlacklist(m);
			DB.removeMovie(m);
			
			for (Media md : m.getMediaList())
				MovieIdentifier.get().addFile(new FileRepresentation(md.getFilepath(), md.getFilepath(), 0));
		}
		catch (Exception e) {
			return buildErrorMessage("Error occured when blacklisting movie");
		}
		
		
		return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
	}

	private String getTitleFilename(String playerName) {
		try {
			return StringUtils.trim(VLCDistributor.get().getTitle(playerName));
		} catch (VLCConnectionNotFoundException e) {
			return StringUtils.EMPTY; 			
		}		
	}
	
	private JukeboxResponse isPlaying(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestIsPlaying args = JukeboxRequestIsPlaying.parseFrom(data);

		Log.Debug(String.format("Getting is playing status on %s...", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			boolean isPlaying = VLCDistributor.get().isPlaying(args.getPlayerName());

			JukeboxResponseIsPlaying r = JukeboxResponseIsPlaying.newBuilder().setIsPlaying(isPlaying).build();
				
			return JukeboxResponse.newBuilder()
					.setType(JukeboxRequestType.IsPlaying)
					.setArguments(r.toByteString())
					.build();
				
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
		}		
	}
	
	private JukeboxResponse suspend(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestSuspend args = JukeboxRequestSuspend.parseFrom(data);

		Log.Debug(String.format("Suspending computer with player %s...", args.getPlayerName()), Log.LogType.COMM);
		
		try {
			if (VLCDistributor.get().suspend(args.getPlayerName()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target control service"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target control service"); 
			
		}				
	}
	
	
}
