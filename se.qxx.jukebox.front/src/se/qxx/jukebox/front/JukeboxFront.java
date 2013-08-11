package se.qxx.jukebox.front;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.protobuf.RpcCallback;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.front.model.Model;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class JukeboxFront extends JFrame implements MovieStatusListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1556370601254211254L;
	
	private static final int SERVER_PORT = 2152;
	private static final String SERVER_IP_ADDRESS = "192.168.1.120";
	private static final String LOG4J_PROPS = "log4j.prop";
	
	public static Logger log;
	private CountDownLatch waiter = new CountDownLatch(1);
		
	MovieCarousel mainCarousel;
	JukeboxMediaPlayer player;
	
	public JukeboxFront() {
		super("Jukebox Front");
		
		String workingDir = System.getProperty("user.dir");
		System.out.println(workingDir);
		initLogging();
		connect();
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
//	    ImageCanvas c = new ImageCanvas("/res/xperiencebg.jpg");
//	    c.setBackground(Color.black);
	    
//	    JPanel p = new JPanel();
//	    p.setLayout(new BorderLayout());
//	    p.add(c, BorderLayout.CENTER);
	    
//	    BackDrop bd = new BackDrop();
//	    bd.setLayout(new BorderLayout());
    
	    player = new JukeboxMediaPlayer(this);
	    player.addKeyListener(this);
	    
//	    int size = Model.get().getMovies().size();
//	    String[] imageUrls = new String[size];
//	    for (int i=0;i<size;i++) 
//	    	imageUrls[i] = String.format("/res/test/movie%s.jpg", i);
	    	    
	    try {
	    	log.info("Querying jukebox server for movies");
			waiter.await();
			
			mainCarousel = new MovieCarousel("/res/xperiencebg.jpg", Model.get().getMovies());
			mainCarousel.setMovieStatusListener(this);
			mainCarousel.addKeyListener(this);
	    	
		    //frame = new JFrame("Jukebox Front");	
		    //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
		    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    this.setContentPane(mainCarousel);
		    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		    this.setUndecorated(true);
		    this.setVisible(true);
		    this.addKeyListener(this);
		} catch (InterruptedException e) {
			System.exit(-1);
		}
	    
	}

	private void connect() {
		if (!Model.get().isInitialized()) {
			final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(SERVER_IP_ADDRESS, SERVER_PORT);
			
			try {
				jh.listMovies("", new RpcCallback<JukeboxResponseListMovies>() {
	
					@Override
					public void run(JukeboxResponseListMovies response) {
			  			Model.get().clearMovies();
						Model.get().addAllMovies(response.getMoviesList());
						Model.get().setInitialized(true);
						waiter.countDown();
					}
				});
			}
			catch (Exception e) {
				log.error("Error connecting to Jukebox server", e);
			}
		}
			
	}
	
	private void initLogging() {
		PropertyConfigurator.configure(LOG4J_PROPS);
		this.log = Logger.getLogger(this.getClass());
	}

	@Override
	public void stop() {
		player.stop();
		changePanel(mainCarousel);
	}

	@Override
	public void play(String filename) {
		// TODO Auto-generated method stub
		changePanel(player);
//		player.initialize(this);

		try {
			if (!player.start(filename)) {
				this.stop();
			}
		}
		catch (Exception e) {
			JukeboxFront.log.error("Error when starting video", e);
			changePanel(player);
		}		
	}
	private void changePanel(JPanel panel) {
		this.getContentPane().removeAll();
		this.setContentPane(panel);
		this.validate();
		this.repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (player.isPlaying()) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				this.stop();
			}					
		}
		else {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				Movie m = Model.get().getMovie(mainCarousel.getCurrentIndex());
				
				JukeboxFront.log.info(m.getMedia(0).getFilename());
				String filepath = m.getMedia(0).getFilepath().replace("/c/media/BitTorrent", "");
				if (filepath.startsWith("/"))
					filepath = filepath.substring(1);
				
				String filename = String.format("\\\\ULTRA\\media\\BitTorrent\\%s\\%s", filepath, m.getMedia(0).getFilename());
				
				this.play(filename);
			}			
		}
			
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
