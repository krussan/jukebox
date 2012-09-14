package se.qxx.hibernator;

import java.util.Arrays;
import java.util.List;

public class Hibernator {

	public static void main(String[] args) {
		HibernatorTcpServer server;
		if (args.length > 0) {
			server = new HibernatorTcpServer(Integer.valueOf(args[0]));
		}
		else {
			server = new HibernatorTcpServer();
		}
	
		System.out.println("Starting tcp server...");
		Thread t = new Thread(server);
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}
}
