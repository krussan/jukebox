import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import com.sun.jna.Native;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
public class JukeboxFront {

	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	public JukeboxFront() {
		setupMain();
		//testEmbeddedPlayer();
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

	private void setupMain() {
	    java.net.URL imgURL = getClass().getResource("res/xperiencebg.jpg");

	    ImageCanvas c = new ImageCanvas("res/xperiencebg.jpg");
	    c.setBackground(Color.black);
	    
	    JPanel p = new JPanel();
	    p.setLayout(new BorderLayout());
	    p.add(c, BorderLayout.CENTER);
	    
    	Image image = Toolkit.getDefaultToolkit().createImage(imgURL);
    	
	    final JFrame frame = new JFrame("Jukebox Front");	
	    //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setContentPane(p);
	    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    frame.setUndecorated(true);
	    frame.setVisible(true);
	}

}
