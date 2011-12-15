package se.qxx.jukebox.tests;

import java.io.Console;
import java.util.ArrayList;

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
			ArrayList<String> subFiles = new ArrayList<String>();
			subFiles.add("file://Y:/Videos/Repo Men.srt");
			conn.enqueue("file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4", subFiles);
			conn.toggleFullscreen();
		}
		
	}
	
	private static void printHelp() {
		System.out.println("TestVLCConnection <host> <port>");
	}
}
