package se.qxx.android.jukebox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestPauseMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStopMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSuspend;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestToggleFullscreen;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestWakeup;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseError;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;

public class JukeboxConnectionHandler implements Runnable {
	
	public String responseMessage;
	public Boolean success;
	
	private JukeboxRequestType _type;
	private Handler _handler;
	private Object[] arguments;
	
	public JukeboxConnectionHandler(Handler h, JukeboxRequestType t, Object... args) {
		this._type = t;
		this._handler = h;
		this.arguments = args;
	}
	
	public void run() {
		Bundle b = new Bundle();
		
		switch (this._type.getNumber()) {
		case JukeboxRequestType.ListMovies_VALUE:
			b = listMovies();
			break;
		case JukeboxRequestType.StartMovie_VALUE:
			b = startMovie();
			break;
		case JukeboxRequestType.StopMovie_VALUE:
			b = stopMovie();
			break;
		case JukeboxRequestType.PauseMovie_VALUE:
			b = pauseMovie();
			break;
		case JukeboxRequestType.Wakeup_VALUE:
			b = wakeup();
			break;
		case JukeboxRequestType.ToggleFullscreen_VALUE:
			b = toggleFullscreen();
			break;
		case JukeboxRequestType.Suspend_VALUE:
			b = suspend();
			break;
		case JukeboxRequestType.ListPlayers_VALUE:
			b = listPlayers();
			break;
		case JukeboxRequestType.Seek_VALUE:
			b = seek();
			break;
		default:
		}
		
		Message m = new Message();
		m.setData(b);
		
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

	private Bundle sendAndRetreive(JukeboxRequestType type, com.google.protobuf.GeneratedMessage message) {
    	Logger.Log().i("opening socket");
    	
    	//TODO: configure server address or display a list of possible servers
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
	
	private void handleResponse(JukeboxRequestType type, ByteString data) {
    	try {
    		switch (type) {
    		case ListMovies:
    			JukeboxResponseListMovies resp1 = JukeboxResponseListMovies.parseFrom(data);
    			Model.get().clearMovies();
    			Model.get().addAllMovies(resp1.getMoviesList());
    			break;
    		case ListPlayers:
    			JukeboxResponseListPlayers resp2 = JukeboxResponseListPlayers.parseFrom(data);
    			Model.get().clearPlayers();
    			Model.get().addAllPlayers(resp2.getHostnameList());
    			break;
    		case MarkSubtitle:
    			break;
    		case SkipBackwards:
    			break;
    		case SkipForward:
    			break;
    		case StartMovie:
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
