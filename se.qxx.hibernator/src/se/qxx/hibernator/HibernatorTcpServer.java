package se.qxx.hibernator;

import java.net.ServerSocket;
import java.net.Socket;

public class HibernatorTcpServer implements Runnable {
	
	
	private boolean isRunning = false;
	private int port = 2148;
	
	public HibernatorTcpServer() {	
	}

	public HibernatorTcpServer(int port) {
		this.port = port;
	}
	@Override
	public void run() {
		isRunning = true;
		// TODO Auto-generated method stub
		try {
			//TODO: Add port to startup arguments
			ServerSocket socket = new ServerSocket(this.port);
			socket.setSoTimeout(500);
			
			while (isRunning) {
				try {
					Socket client = socket.accept();
					
					Thread t = new Thread(new HibernatorTcpConnection(this, client));
					t.start();
				}
				catch (Exception e) {
				}
			}
		}
		catch (Exception e) {
			
		}
	}
	
	public void stop() {
		this.isRunning = false;
	}

}
