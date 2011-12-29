package se.qxx.jukebox;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.sql.SQLException;
import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponse;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class TcpConnection implements Runnable {
	private Socket _client;
	
	public TcpConnection(Socket client) {
		this._client = client;
	}
	
	@Override
	public void run() {
		try {
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
			
			JukeboxResponse resp = handleRequest(req);
			
			if (resp != null) {
				OutputStream os = this._client.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeInt(resp.getSerializedSize());
				
				resp.writeTo(os);
			}

		} catch (IOException e) {
			Log.Error("Error while reading request from client", e);
		}
	}

	private JukeboxResponse handleRequest(JukeboxRequest req) throws InvalidProtocolBufferException {
		try {
			switch (req.getType()) {			
			case ListMovies:
				return listMovies(req);
			default:
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.Error("Error while sending response", e);
		}
		
		return null;
	}

	private JukeboxResponse listMovies(JukeboxRequest req) throws IOException {
		ByteString data = req.getArguments();
		JukeboxRequestListMovies args = JukeboxRequestListMovies.parseFrom(data);
		
		String searchString = args.getSearchString();
		try {
			List<Movie> list = DB.searchMovies(searchString);

			JukeboxResponseListMovies lm = JukeboxResponseListMovies.newBuilder().addAllMovies(list).build();
	    	java.io.ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    	lm.writeTo(bos);
	    	
			JukeboxResponse resp = JukeboxResponse.newBuilder()
					.setType(JukeboxRequestType.ListMovies)
					.setArguments(lm.toByteString())
					.build();
					
			return resp;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
