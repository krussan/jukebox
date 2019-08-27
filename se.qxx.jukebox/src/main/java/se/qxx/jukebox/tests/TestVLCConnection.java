package se.qxx.jukebox.tests;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.factories.VLCConnectionFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IVLCConnection;
import se.qxx.jukebox.vlc.VLCConnection;

public class TestVLCConnection {

	private IJukeboxLogger log;
	private VLCConnectionFactory vlcConnectionFactory;

	@Inject
	public TestVLCConnection(LoggerFactory factory, VLCConnectionFactory vlcConnectionFactory) {
		this.setVlcConnectionFactory(vlcConnectionFactory);
		this.log = factory.create(LogType.COMM);
	}
	
	public VLCConnectionFactory getVlcConnectionFactory() {
		return vlcConnectionFactory;
	}

	public void setVlcConnectionFactory(VLCConnectionFactory vlcConnectionFactory) {
		this.vlcConnectionFactory = vlcConnectionFactory;
	}

	public static void main(String[] args) {
		if (args.length < 2)
			printHelp();
		else
		{
			Injector injector = Binder.setupBindings(args);
			TestVLCConnection prog = injector.getInstance(TestVLCConnection.class);
			prog.execute(args[0], Integer.parseInt(args[1]));
			
		}
		 
	}
	
	private static void printHelp() {
		System.out.println("TestVLCConnection <host> <port>");
	}
	
	public void execute(String host, int port) {
		IVLCConnection conn = this.getVlcConnectionFactory().create(host, port);
		
		String subfile = "file://Y:/Videos/Repo Men.srt";
		conn.enqueue("file://Y:/Videos/Kick.Ass[2010]DVD.ENG.X264.mp4", subfile);
		conn.toggleFullscreen();
	}
}
