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

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestPauseMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStopMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestToggleFullscreen;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestWakeup;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseError;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
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
		
		List<Movie> list = DB.searchMovies(searchString);

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
			if (VLCDistributor.get().startMovie(args.getPlayerName(), args.getMovieId()))
				return JukeboxResponse.newBuilder().setType(JukeboxRequestType.OK).build();
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
	
	private JukeboxResponse buildErrorMessage(String errorMessage) {
		// TODO Auto-generated method stub
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
}
