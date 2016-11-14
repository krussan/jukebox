package se.qxx.jukebox.tests;

import se.qxx.jukebox.vlc.*;

public class TestVLCConnection {

	public static void main(String[] args) {
		String host;
		int port;
		if (args.length < 2)
			printHelp();
		else
		{
			host = args[0];
			port = Integer.parseInt(args[1]);
					
			VLCConnection conn = new VLCConnection(host, port);
			
			String subfile = "file://Y:/Videos/Repo Men.srt";
			conn.enqueue("file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4", subfile);
			conn.toggleFullscreen();
		}
		 
	}
	
	private static void printHelp() {
		System.out.println("TestVLCConnection <host> <port>");
	}
}
