package se.qxx.jukebox.front;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.front.model.Model;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


public class JukeboxMediaPlayer extends JPanel {
	
	Canvas movieCanvas = new Canvas();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6294647093162238024L;
	EmbeddedMediaPlayer mediaPlayerComponent;
	
	public JukeboxMediaPlayer(JFrame frame) {
		movieCanvas.setBackground(Color.BLACK);
	    
	    this.setLayout(new BorderLayout());
	    this.add(movieCanvas, BorderLayout.CENTER);
	    
	    String[] VLC_ARGS = {
	            "--intf", "dummy",          // no interface
//	            "--vout", "dummy",          // we don't want video (output)
//	            "--no-audio",               // we don't want audio (decoding)
	            "--no-video-title-show",    // nor the filename displayed
	            "--no-stats",               // no stats
	            "--no-sub-autodetect-file", // we don't want subtitles
//	            "--no-inhibit",             // we don't want interfaces
	            "--no-disable-screensaver", // we don't want interfaces
	            "--no-snapshot-preview",    // no blending in dummy vout
//	            "--verbose", "2",
//	            "--file-logging",
//	            "--logfile", "c:\\temp\\vlc.log"
	    };
	    
	    MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
	    mediaPlayerComponent = factory.newEmbeddedMediaPlayer(new DefaultFullScreenStrategy(frame));
		
	    mediaPlayerComponent.setVideoSurface(factory.newVideoSurface(movieCanvas));	    
	}
	

	public boolean start(String filename) {
			//JukeboxFront.log.info(m.getMedia(0).getFilename());
//			String filepath = m.getMedia(0).getFilepath().replace("/c/media/BitTorrent", "");
//			if (filepath.startsWith("/"))
//				filepath = filepath.substring(1);
//			
//			String filename = String.format("\\\\ULTRA\\media\\BitTorrent\\%s\\%s", filepath, m.getMedia(0).getFilename());
			
			JukeboxFront.log.info(filename);
			boolean success = mediaPlayerComponent.startMedia(filename);
			
			this.requestFocusInWindow();
			
			return success;

		//mediaPlayerComponent.startMedia(arg0, StringUtils.EMPTY)
	}
	
	public void stop() {
		mediaPlayerComponent.stop();
//		mediaPlayerComponent.release();
	}
	
	public boolean isPlaying() {
		return mediaPlayerComponent.isPlaying();		
	}
	
}
