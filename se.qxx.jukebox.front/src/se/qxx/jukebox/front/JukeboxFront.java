package se.qxx.jukebox.front;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.protobuf.RpcCallback;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.front.comm.TcpListener;
import se.qxx.jukebox.front.model.Model;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class JukeboxFront extends JFrame implements MovieStatusListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1556370601254211254L;
	
	private static final String LOG4J_PROPS = "log4j.prop";
	
	public static Logger log;
	private CountDownLatch waiter = new CountDownLatch(1);
	private TcpListener _listener;
	
	MovieCarousel mainCarousel;
	JukeboxMediaPlayer player;
	
	public JukeboxFront() {
		super("Jukebox Front");
		
		String workingDir = System.getProperty("user.dir");
		System.out.println(workingDir);
		initLogging();
		connect();
		setupMain();
		setupListening();
		//testEmbeddedPlayer();
		//testFullscreen();		
	}
	
	public static final void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		Arguments.parseCLI(args);
		
		if (checkArgs()) {
			initialize();
		}
		
	}

	private static boolean checkArgs() {
		if (Arguments.cmd().hasOption("help")) {
			Arguments.get().printHelp();
			return false;
		}
			
		return true;
	}
	
	private static void initialize() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), FrontSettings.get().getLibVlcPath());
 
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
		    
		    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    this.setContentPane(mainCarousel);
		    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		    this.addKeyListener(this);

		    //this.setUndecorated(true);
		    //this.setVisible(true);

		    //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
		    setFullscreen();
		} catch (InterruptedException e) {
			System.exit(-1);
		}
	    
	}

	private void connect() {
		if (!Model.get().isInitialized()) {
			final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
					FrontSettings.get().getServer(), 
					FrontSettings.get().getServerPort());
			
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
		JukeboxFront.log = Logger.getLogger(this.getClass());
	}

	@Override
	public void stop() {
		player.stop();
		changePanel(mainCarousel);
	}

	@Override
	public void play(String filename) {
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
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			startStop();
		
	}
	
	private void startStop() {
		if (player.isPlaying()) {
				this.stop();
		}
		else {
			Movie m = Model.get().getMovie(mainCarousel.getCurrentIndex());
			
			JukeboxFront.log.info(m.getMedia(0).getFilename());
			String filepath = m.getMedia(0).getFilepath().replace("/c/media/BitTorrent", "");
			if (filepath.startsWith("/"))
				filepath = filepath.substring(1);
			
			String filename = String.format("\\\\ULTRA\\media\\BitTorrent\\%s\\%s", filepath, m.getMedia(0).getFilename());
			
			this.play(filename);
		}
	}
	
	private void setupListening() {
		try {
			_listener = new TcpListener();
			Thread t = new Thread(_listener);
			t.start();
		}
		catch (Exception e) {
			
		}
	}
	
	@SuppressWarnings("unused")
	private void stopListening() {
		try {
			_listener.stopListening();
		}
		catch (Exception e) {
			
		}
	}		
	
	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	private void setFullscreen() {
	 final GraphicsDevice gd = this.getGraphicsConfiguration().getDevice();
	      log.debug(SystemUtils.OS_NAME);
	      log.debug(SystemUtils.OS_VERSION);
	      log.debug(SystemUtils.OS_ARCH);
	      log.debug(System.getProperty("linux.version", ""));

	      this.setUndecorated( true );
	      this.setResizable(true);

	      boolean isFullScreenSupported = gd.isFullScreenSupported();
	      if (StringUtils.containsIgnoreCase(System.getProperty("linux.version", ""), "ubuntu"))
		  isFullScreenSupported = true;

	      if (isFullScreenSupported) {
		  log.debug("- Creating UI window (native fullscreen)");
		  gd.setFullScreenWindow(this);
	      }
	      else {
		  final Rectangle bounds = this.getGraphicsConfiguration().getBounds(); 
		  log.debug("- Creating UI window (fullscreen fallback for " + bounds + ")");
		  this.setAlwaysOnTop( true );
		  this.setBounds( bounds );
		  this.setVisible( true );
	      }
	}
}
