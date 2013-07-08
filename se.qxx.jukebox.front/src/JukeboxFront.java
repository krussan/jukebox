import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
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

		NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), "D:/Dev/svn/jukebox/lib/native/win");
 
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

	    ImageCanvas c = new ImageCanvas("/res/xperiencebg.jpg");
	    c.setBackground(Color.black);
	    
	    JPanel p = new JPanel();
	    p.setLayout(new BorderLayout());
	    p.add(c, BorderLayout.CENTER);
	    
	    BackDrop bd = new BackDrop();
	    bd.setLayout(new BorderLayout());
	    //bd.add(c, BorderLayout.CENTER);
	    String[] imageUrls = new String[9];
	    for (int i = 1;i<=9;i++) 
	    	imageUrls[i-1] = String.format("/res/test/movie%s.jpg", i);
	    
	    Carousel cl = new Carousel("/res/xperiencebg.jpg", imageUrls);
	    bd.setLayout(new BorderLayout());

    	
	    final JFrame frame = new JFrame("Jukebox Front");	
	    //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setContentPane(cl);
	    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    frame.setUndecorated(true);
	    frame.setVisible(true);
	}

}
