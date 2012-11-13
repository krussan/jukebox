package se.qxx.jukebox; 

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

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
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Vlc.Server;
import se.qxx.jukebox.vlc.VLCConnectionNotFoundException;
import se.qxx.jukebox.vlc.VLCDistributor;

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
			if (VLCDistributor.get().startMovie(args.getPlayerName(), args.getMovieId())) {
				//TODO: MEDIA -- Fix this
				List<Subtitle> subs = new ArrayList<Subtitle>();// DB.getSubtitles(args.getMovieId());
				
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

		Log.Debug(String.format("Getting list of subtitles form movie ID :: %s", args.getMovieId()), Log.LogType.COMM);

		//TODO: MEDIA -- Fix this 
		List<Subtitle> subs = new ArrayList<Subtitle>(); //DB.getSubtitles(args.getMovieId());
	
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
		
		try {
			if (VLCDistributor.get().setSubtitle(args.getPlayerName(), args.getSubtitleID()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
			else
				return buildErrorMessage("Error occured when connecting to target media player"); 
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
		}		
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
				JukeboxResponseTime time = JukeboxResponseTime.newBuilder().setSeconds(seconds).build();
				
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
		
		try {
			String response = StringUtils.trim(VLCDistributor.get().getTitle(args.getPlayerName()));
			JukeboxResponseGetTitle r = JukeboxResponseGetTitle.newBuilder().setTitle(response).build();
				
			return JukeboxResponse.newBuilder()
					.setType(JukeboxRequestType.GetTitle)
					.setArguments(r.toByteString())
					.build();
				
		} catch (VLCConnectionNotFoundException e) {
			return buildErrorMessage("Error occured when connecting to target media player"); 
			
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
