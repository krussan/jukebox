package se.qxx.hibernator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class HibernatorTcpConnection implements Runnable {

	Socket client = null;
	HibernatorTcpServer server = null;
	
	public HibernatorTcpConnection(HibernatorTcpServer server, Socket client) {
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void run() {
		System.out.println(String.format("Connection made from %s", client.getInetAddress().toString()));
		
		try {
			InputStream is = this.client.getInputStream();

			char[] data = new char[500];
			int offset = 0;
			int numRead = 0;
			StringBuilder sb = new StringBuilder();
			
			Reader r = new InputStreamReader(is, "utf-8");
						
			while((numRead = r.read(data, offset, data.length)) >= 0) {
				sb.append(data, 0, numRead);	
			}
			String inputString = sb.toString();
			String[] commands = inputString.split(";");
			
			for (String command : commands) {
				executeCommand(command);
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void executeCommand(String command) {
		String commandLowerCase = command.toLowerCase();
		
		System.out.println(String.format("Executing command :: %s", command));
		if (commandLowerCase.equals("hibernate")) {
			try {
				HibernationFactory.Create().hibernate();
			} catch (HibernationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if (commandLowerCase.equals("suspend"))
		{
			try {
				HibernationFactory.Create().suspend();
			} catch (HibernationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;			
		}
		
		if (commandLowerCase.equals("stop")) {
			this.server.stop();
		}
	}

}
