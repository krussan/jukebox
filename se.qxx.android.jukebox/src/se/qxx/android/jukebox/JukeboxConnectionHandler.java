package se.qxx.android.jukebox;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import se.qxx.android.jukebox.MovieProto.JukeboxRequest;
import se.qxx.android.jukebox.MovieProto.JukeboxRequestListMovies;
import se.qxx.android.jukebox.MovieProto.JukeboxRequestType;
import se.qxx.android.jukebox.MovieProto.JukeboxResponseListMovies;
import se.qxx.android.tools.Logger;

public class JukeboxConnectionHandler implements Runnable {
	
	public String responseMessage;
	public Boolean success;
	
	private JukeboxRequestType _type;
	private Handler _handler;
	
	public JukeboxConnectionHandler(Handler h, MovieProto.JukeboxRequestType t) {
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
    	Logger.Log().i("opening socket");
    	
    	//TODO: configure server address or display a list of possible servers
    	java.net.Socket s = new java.net.Socket();
    	
 
    	
    	try {
	    	s.connect(new InetSocketAddress("192.168.0.181", 45444), 5000);
	    	
	    	Logger.Log().i("socket opened");
	    	Logger.Log().i("sending test message");
	    	
	    	JukeboxRequestListMovies lm = JukeboxRequestListMovies.newBuilder().setSearchString("").build();
	    	java.io.ByteArrayOutputStream os = new ByteArrayOutputStream();
	    	lm.writeTo(os);
	    
	    	JukeboxRequest req = JukeboxRequest.newBuilder().setType(JukeboxRequestType.ListMovies)
	    			//.setArguments(ByteString.copyFrom(os.toByteArray()))
	    			.setArguments(lm.toByteString())
	    			.build();
	    	
	    	req.writeTo(s.getOutputStream());
	    	s.shutdownOutput();
	    	
	    	Logger.Log().i("waiting for response...");
	    	
	    	JukeboxResponseListMovies resp = JukeboxResponseListMovies.parseFrom(s.getInputStream());
	    	
	    	Logger.Log().i("response read");
	    	
	    	if (resp == null)
	    		Logger.Log().i("...but response is null...");
	    	
	    	s.close();
	    	
	    	return setResponse(true);
    	}
    	catch (java.net.SocketTimeoutException ex) {
    		return setResponse(false, "Application was unable to connect to server. Check server settings.");    		    	
    	}
    	catch (java.io.IOException ioException)
    	{
    		return setResponse(false, "Application was unable to connect to server. Check server settings.");    		
    	}	
    	
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
