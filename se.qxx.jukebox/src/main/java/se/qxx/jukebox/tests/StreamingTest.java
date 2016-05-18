package se.qxx.jukebox.tests;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

public class StreamingTest {
	 public static void main(String[] args) throws Exception {
	        if(args.length != 1) {
	            System.out.println("Specify a single MRL to stream");
	            System.exit(1);
	        }

	        String media = args[0];
	        String options = formatRtpStream("192.168.0.0", 5555);

	        System.out.println("Streaming '" + media + "' to '" + options + "'");

	        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(args);
	        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();

	        mediaPlayer.playMedia(media,
	            options,
	            ":no-sout-rtp-sap",
	            ":no-sout-standard-sap",
	            ":sout-all",
	            ":sout-keep"
	        );

	        // Don't exit
	        Thread.currentThread().join();
	    }

	    private static String formatRtpStream(String serverAddress, int serverPort) {
	        StringBuilder sb = new StringBuilder(60);
	        sb.append(":sout=#rtp{dst=");
	        sb.append(serverAddress);
	        sb.append(",port=");
	        sb.append(serverPort);
	        sb.append(",mux=ts}");
	        return sb.toString();
	    }
}
