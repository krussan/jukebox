package se.qxx.android.jukebox.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestBlacklistMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestMarkSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestPauseMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSetSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStopMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSuspend;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestTime;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestToggleFullscreen;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestWakeup;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseError;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class JukeboxConnectionHandler implements Runnable {
	
	public String responseMessage;
	public Boolean success;
	
	private JukeboxRequestType _type;
	private Handler _handler;
	private Object[] arguments;
	private JukeboxResponseListener listener;
	
	public JukeboxConnectionHandler(Handler h, JukeboxRequestType t, Object... args) {
		this._type = t;
		this._handler = h;
		this.arguments = args;
	}

	public JukeboxConnectionHandler(JukeboxResponseListener listener, JukeboxRequestType t, Object... args) {
		this._type = t;
		this.listener = listener;
		this.arguments = args;
	}
	
	public void run() {
		Bundle b = new Bundle();
		
		switch (this._type) {
		case ListMovies:
			b = listMovies();
			break;
		case StartMovie:
			b = startMovie();
			break;
		case StopMovie:
			b = stopMovie();
			break;
		case PauseMovie:
			b = pauseMovie();
			break;
		case Wakeup:
			b = wakeup();
			break;
		case ToggleFullscreen:
			b = toggleFullscreen();
			break;
		case Suspend:
			b = suspend();
			break;
		case ListPlayers:
			b = listPlayers();
			break;
		case Seek:
			b = seek();
			break;
		case ListSubtitles:
			b = listSubtitles();
			break;
		case MarkSubtitle:
			b = markSubtitle();
			break;
		case IsPlaying:
			b = isPlaying();
			break;
		case GetTitle:
			b = getTitle();
			break;
		case Time:
			b = getTime();
			break;
		case SetSubtitle:
			b = setSubtitle();
			break;
		case BlacklistMovie:
			b = blacklist();
			break;
		default:
		}
				
		Message m = new Message();
		m.setData(b);
		
		if (this._handler != null)
			this._handler.sendMessage(m);
	}
	 
	private Bundle startMovie() {
		JukeboxRequestStartMovie sm = JukeboxRequestStartMovie.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.setMovieId(Model.get().getCurrentMovie().getID())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.StartMovie, sm);
	}
	
	private Bundle stopMovie() {
		JukeboxRequestStopMovie sm = JukeboxRequestStopMovie.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.StopMovie, sm);
	}
	
	private Bundle pauseMovie() {
		JukeboxRequestPauseMovie sm = JukeboxRequestPauseMovie.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.PauseMovie, sm);		
	}
	
	private Bundle listMovies() {
		JukeboxRequestListMovies lm = JukeboxRequestListMovies.newBuilder()
				.setSearchString("")
				.build();
		
		return sendAndRetreive(JukeboxRequestType.ListMovies, lm);    	
	}
	
	private Bundle wakeup() {
		JukeboxRequestWakeup sm = JukeboxRequestWakeup.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.Wakeup, sm);		
	}
	
	private Bundle toggleFullscreen() {
		JukeboxRequestToggleFullscreen sm = JukeboxRequestToggleFullscreen.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.ToggleFullscreen, sm);		
	}

	private Bundle isPlaying() {
		JukeboxRequestIsPlaying sm = JukeboxRequestIsPlaying.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.IsPlaying, sm);		
	}

	private Bundle getTime() {
		JukeboxRequestTime sm = JukeboxRequestTime.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.Time, sm);		
	}
	
	private Bundle getTitle() {
		JukeboxRequestGetTitle sm = JukeboxRequestGetTitle.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.GetTitle, sm);		
	}
	
	private Bundle suspend() {
		JukeboxRequestSuspend sm = JukeboxRequestSuspend.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.Suspend, sm);		
	}

	private Bundle listPlayers() {
		JukeboxRequestListPlayers sm = JukeboxRequestListPlayers.newBuilder()
				.build();
		
		return sendAndRetreive(JukeboxRequestType.ListPlayers, sm);		
	}
	
	private Bundle listSubtitles() {
		JukeboxRequestListSubtitles sm = JukeboxRequestListSubtitles.newBuilder()
				.setMediaId(Model.get().getCurrentMedia().getID())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.ListSubtitles, sm);
	}
	
	private Bundle seek() {
		if (this.arguments.length > 0)
		{
			if (this.arguments[0] instanceof Integer) {
				int seconds = (Integer)this.arguments[0];
				JukeboxRequestSeek sm = JukeboxRequestSeek.newBuilder()
						.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
						.setSeconds(seconds)
						.build();
				
				return sendAndRetreive(JukeboxRequestType.Seek, sm);
			}
		}
		
		return new Bundle();		
	}	

	private Bundle setSubtitle() {
		JukeboxRequestSetSubtitle sm = JukeboxRequestSetSubtitle.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.setMediaID(Model.get().getCurrentMedia().getID())
				.setSubtitleDescription(Model.get().getCurrentSubtitle())
				.build();
		
		return sendAndRetreive(JukeboxRequestType.SetSubtitle, sm);
	}	
	
	private Bundle markSubtitle() {
		if (this.arguments.length > 0)
		{
			if (this.arguments[0] instanceof Boolean) {
				boolean subOk= (Boolean)this.arguments[0];
				JukeboxRequestMarkSubtitle sm = JukeboxRequestMarkSubtitle.newBuilder()
						.setIsOk(subOk)
						.build();
				
				return sendAndRetreive(JukeboxRequestType.MarkSubtitle, sm);
			}
		}
		
		return new Bundle();		
	}	

	private Bundle sendAndRetreive(JukeboxRequestType type, com.google.protobuf.GeneratedMessage message) {
    	Logger.Log().i("opening socket");
    	
    	java.net.Socket s = new java.net.Socket();
    	
    	try {
	    	s.connect(new InetSocketAddress(
	    			JukeboxSettings.get().getServerIpAddress(), 
	    			JukeboxSettings.get().getServerPort()), 
	    			5000);
	    	
	    	Logger.Log().i("socket opened");
	    	Logger.Log().i(String.format("Sending message of type %s", type.toString()));
	    	
	    	JukeboxRequest req = getRequest(type, message);
	    	int lengthOfMessage = req.getSerializedSize();
	    	
	    	DataOutputStream dos = new DataOutputStream(s.getOutputStream());
	    	
	    	// write length of message
	    	dos.writeInt(lengthOfMessage);
	    	// write message
	    	req.writeTo(dos);
	    	
	    	Logger.Log().i("waiting for response...");
	    	
	    	JukeboxResponse resp = readResponse(s.getInputStream());
	    	if (resp == null) {
	    		Logger.Log().i("...but response is null...");
	    		return setResponse(false, "Response is null");
	    	}
	    	else {
	    		if (this.listener != null)
	    			this.listener.onResponseReceived(resp);
	    		
		    	ByteString data = resp.getArguments();    	
		    	handleResponse(resp.getType(), data);
		    	
		    	Logger.Log().i("response read");
		    	
		    	s.close();
		    	
	    		if (resp.getType() == JukeboxRequestType.Error)
	    			return setResponse(false, JukeboxResponseError.parseFrom(data).getErrorMessage());
	    		else
	    			return setResponse(true);	    		
	    	}	    	
    	}
    	catch (java.net.SocketTimeoutException ex) {
    		return setResponse(false, "Application was unable to connect to server. Check server settings.");    		    	
    	}
    	catch (java.io.IOException ioException)
    	{
    		return setResponse(false, "Application was unable to connect to server. Check server settings.");    		
    	}	
	}
	
	private Bundle blacklist() {
		if (this.arguments.length > 0) {
			if (this.arguments[0] instanceof Movie) {
				Movie m = (Movie)this.arguments[0];
				JukeboxRequestBlacklistMovie bm = JukeboxRequestBlacklistMovie.newBuilder().setMovieId(m.getID()).build();
				return sendAndRetreive(JukeboxRequestType.BlacklistMovie, bm);
			}
			
		}
		
		return new Bundle();
	}
	
	private void handleResponse(JukeboxRequestType type, ByteString data) {
    	try {
    		switch (type) {
    		case ListMovies:
    			JukeboxResponseListMovies resp1 = JukeboxResponseListMovies.parseFrom(data);
    			Model.get().clearMovies();
    			Model.get().addAllMovies(resp1.getMoviesList());
    			Model.get().setInitialized(true);
    			break;
    		case ListPlayers:
    			JukeboxResponseListPlayers resp2 = JukeboxResponseListPlayers.parseFrom(data);
    			Model.get().clearPlayers();
    			Model.get().addAllPlayers(resp2.getHostnameList());
    			break;
    		case ListSubtitles:
    			JukeboxResponseListSubtitles resp3 = JukeboxResponseListSubtitles.parseFrom(data);
    			Model.get().clearSubtitles();
    			Model.get().addAllSubtitles(resp3.getSubtitleList());
    			break;
    		case MarkSubtitle:
    			break;
    		case SkipBackwards:
    			break;
    		case SkipForward:
    			break;
    		case StartMovie:
    			JukeboxResponseStartMovie resp4 = JukeboxResponseStartMovie.parseFrom(data);
    			Model.get().clearSubtitles();
    			Model.get().addAllSubtitles(resp4.getSubtitleList());
    			break;
    		case StartSubtitleIdentity:
    			break;
    		case StopMovie:
    			break;
    		case Wakeup:
    			break;
    		case ToggleFullscreen:
    			break;
    		case Suspend:
    			break;	
    		case Error:
    			JukeboxResponseError err = JukeboxResponseError.parseFrom(data);
    			Log.e("Jukebox", "Error occured when communicating with jukebox server");
    			Log.e("Jukebox", err.getErrorMessage());
    		}
			
		} catch (InvalidProtocolBufferException e) {
			
			e.printStackTrace();
		}

	}
	private JukeboxRequest getRequest(JukeboxRequestType type, com.google.protobuf.GeneratedMessage message) {
		return JukeboxRequest.newBuilder()
			.setType(type)
			.setArguments(message.toByteString())
			.build();	
	}
	
	private JukeboxResponse readResponse(InputStream is) {
		try {		
			DataInputStream ds = new DataInputStream(is);
			int lengthOfMessage;
			lengthOfMessage = ds.readInt();
	
			byte[] data = new byte[lengthOfMessage];
	
			int offset = 0;
			int numRead = 0;
			while (offset < lengthOfMessage && (numRead = is.read(data, offset, lengthOfMessage - offset)) >= 0) {
				offset += numRead;
			}
			
			return JukeboxResponse.parseFrom(data);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Bundle setResponse(Boolean success) {
		Bundle b = new Bundle();
		b.putBoolean("success", success);
		
		return b;
	}
	
	private Bundle setResponse(Boolean success, String message) {
		Bundle b = setResponse(success);
		b.putString("message", message);
		
		return b;
	}
}
