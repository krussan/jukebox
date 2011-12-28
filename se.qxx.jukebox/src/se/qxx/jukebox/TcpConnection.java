package se.qxx.jukebox;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.sql.SQLException;
import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequest;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
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
			
			handleRequest(req);

		} catch (IOException e) {
			Log.Error("Error while reading request from client", e);
		}
	}

	private void handleRequest(JukeboxRequest req) throws InvalidProtocolBufferException {
		switch (req.getType()) {
		case ListMovies:
			listMovies(req);
			break;
		default:
			break;
		}
	}

	private void listMovies(JukeboxRequest req, OutputStream os) throws InvalidProtocolBufferException {
		ByteString data = req.getArguments();
		JukeboxRequestListMovies args = JukeboxRequestListMovies.parseFrom(data);
		
		String searchString = args.getSearchString();
		try {
			List<Movie> list = DB.searchMovies(searchString);
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
