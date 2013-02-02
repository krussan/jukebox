import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import com.sun.jna.Native;

import se.qxx.jukebox.front.comm.JukeboxConnectionHandler;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
public class JukeboxFront {

	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	public JukeboxFront() {
		testEmbeddedPlayer();
		//testFullscreen();		
	}
	
	public static final void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

 	    Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					new JukeboxFront();
				}
			});
	}
	
	private void testEmbeddedPlayer() {
	    JFrame frame = new JFrame("vlcj Tutorial");
	    
	    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
	    frame.setContentPane(mediaPlayerComponent);
	    
		Container c = frame.getContentPane();
 		c.setBackground(Color.BLACK);

 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setUndecorated(true);
 		frame.setResizable(false);

 		// set undecorated fullscreen window
 		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);		

 		// ensure that the media player fills the whole frame
	    mediaPlayerComponent.getMediaPlayer().setFullScreen(true);
	    
	    // start movie?
	    mediaPlayerComponent.getMediaPlayer().playMedia("file:///media/ultra/BitTorrent/Homeland.S02.Season.2.HDTV.x264-EVOLVE.ASAP/Homeland.S02E05.PROPER.HDTV.x264-EVOLVE.mp4");
	}
	
	private void setupFullscreen() {
 		final JFrame fullscreenFrame = new JFrame();
 		
 		fullscreenFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		fullscreenFrame.setUndecorated(true);
 		fullscreenFrame.setResizable(false);
 		//fullscreenFrame.add(new JLabel("Press ALT+F4 to exit fullscreen.", SwingConstants.CENTER), BorderLayout.CENTER);
 		fullscreenFrame.validate();
 		
 		Container c = fullscreenFrame.getContentPane();
 		c.setBackground(Color.BLACK);
 		
 		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(fullscreenFrame);		
		
	}
}
