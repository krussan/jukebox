package se.qxx.jukebox.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;


public abstract class JukeboxServerConnection implements Runnable {
	
	private Socket client;
	
	protected enum LogLevel {
		Debug,
		Error,
		Critical,
		Info
	}
	
	public JukeboxServerConnection(Socket socket) {
		this.setClient(socket);
	}
	
	protected abstract void Log(LogLevel level, String message);
	protected abstract void Log(LogLevel level, String message,  Exception e);

	public Socket getClient() {
		return client;
	}

	public void setClient(Socket client) {
		this.client = client;
	}
	
	/*
	@Override
	public void run() {
		try { 
			Log(LogLevel.Debug, String.format("Connection made from %s", this.getClient().getInetAddress().toString()));
			// read 4 bytes parsing the message length
			InputStream is = this.getClient().getInputStream();
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
			
			Log(LogLevel.Debug,String.format("Request was of type :: %s", req.getType().toString()));
			JukeboxResponse resp = handleRequest(req);
						
			if (resp != null) {
				OutputStream os = this.getClient().getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeInt(resp.getSerializedSize());
				
				resp.writeTo(os);
			}
			

		} catch (IOException e) {
			Log(LogLevel.Error, "Error while reading request from client", e);
		}
	}

			*/
	
	/*
	private JukeboxResponse handleRequest(JukeboxRequest req) throws InvalidProtocolBufferException { 
		try {
			Method m = this.getClass().getMethod(req.getType().toString(), JukeboxRequest.class);
			
			try {
				m.invoke(this, req);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				Log(LogLevel.Error, "Error while invoking ", e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO* Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	*/
}
