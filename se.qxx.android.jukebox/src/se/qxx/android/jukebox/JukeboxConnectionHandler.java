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

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;

import se.qxx.android.tools.Logger;

public class JukeboxConnectionHandler implements Runnable {
	
	public String responseMessage;
	public Boolean success;
	
	private JukeboxRequestType _type;
	private Handler _handler;
	
	public JukeboxConnectionHandler(Handler h, JukeboxRequestType t) {
		this._type = t;
		this._handler = h;
	}
	
	public void run() {
		Bundle b = new Bundle();
		
		switch (this._type.getNumber()) {
		case JukeboxRequestType.ListMovies_VALUE:
			b = listMovies();
			break;
		default:
		}
		
		Message m = new Message();
		m.setData(b);
		
		this._handler.sendMessage(m);
	}
	
	public Bundle listMovies() {
		JukeboxRequestListMovies lm = JukeboxRequestListMovies.newBuilder()
				.setSearchString("")
				.build();
		
		return sendAndRetreive(JukeboxRequestType.ListMovies, lm);    	
	}
	
	private Bundle sendAndRetreive(JukeboxRequestType type, com.google.protobuf.GeneratedMessage message) {
    	Logger.Log().i("opening socket");
    	
    	//TODO: configure server address or display a list of possible servers
    	java.net.Socket s = new java.net.Socket();
    	
    	try {
	    	s.connect(new InetSocketAddress(
	    			JukeboxSettings.get().get_serverIpAddress(), 
	    			JukeboxSettings.get().get_serverPort()), 
	    			5000);
	    	
	    	Logger.Log().i("socket opened");
	    	Logger.Log().i("sending test message");
	    	
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
    			JukeboxResponseListMovies resp = JukeboxResponseListMovies.parseFrom(data);
    			Model.get().addAllMovies(resp.getMoviesList());
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
    		}
			
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private JukeboxRequest getRequest(JukeboxRequestType type, com.google.protobuf.GeneratedMessage message) {
		return JukeboxRequest.newBuilder()
			.setType(JukeboxRequestType.ListMovies)
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
			// TODO Auto-generated catch block
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
