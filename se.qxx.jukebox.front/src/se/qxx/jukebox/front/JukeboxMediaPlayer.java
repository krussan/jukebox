package se.qxx.jukebox.front;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


public class JukeboxMediaPlayer extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6294647093162238024L;
	EmbeddedMediaPlayer mediaPlayerComponent;
	
	public void initialize(JFrame frame) {
	    Canvas c = new Canvas();
	    c.setBackground(Color.black);
	    
	    this.setLayout(new BorderLayout());
	    this.add(c, BorderLayout.CENTER);
	    
	    MediaPlayerFactory factory = new MediaPlayerFactory();
	    mediaPlayerComponent = factory.newEmbeddedMediaPlayer(new DefaultFullScreenStrategy(frame));
		        
	    mediaPlayerComponent.setVideoSurface(factory.newVideoSurface(c));

	}
	
	public void start(Movie m) {
		JukeboxFront.log.info(m.getMedia(0).getFilename());
		//mediaPlayerComponent.startMedia(arg0, StringUtils.EMPTY)
	}
}
