import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


public class JukeboxMediaPlayer {
	
	private void testEmbeddedPlayer() {
	    Canvas c = new Canvas();
	    c.setBackground(Color.black);
	    
	    JPanel p = new JPanel();
	    p.setLayout(new BorderLayout());
	    p.add(c, BorderLayout.CENTER);
	    
	    	

	    final JFrame frame = new JFrame("vlcj Tutorial");	
	    //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setContentPane(p);
	    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    frame.setUndecorated(true);
	    //frame.setSize(800, 600);
	    
	    frame.setVisible(true);
	    
	    
	    
	    MediaPlayerFactory factory = new MediaPlayerFactory();
	    EmbeddedMediaPlayer mediaPlayerComponent = factory.newEmbeddedMediaPlayer(new DefaultFullScreenStrategy(frame));
		        
	    mediaPlayerComponent.setVideoSurface(factory.newVideoSurface(c));

	}
}
