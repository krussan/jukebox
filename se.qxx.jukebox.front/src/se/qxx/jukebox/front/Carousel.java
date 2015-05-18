package se.qxx.jukebox.front;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Timer;
import javax.swing.JPanel;

import se.qxx.jukebox.front.input.KeyListenerWrapper;
import se.qxx.jukebox.front.input.T9;



public class Carousel extends JPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5859498040090776572L;
	
	private Thread animator;
	private Thread rotater;
	
	private final int DELAY = 1;
	
	MediaTracker tracker;
	LogListener logListener;
		
	private CarouselImage[] images;
	
//	private final long KEY_DELAY = 500;
	
	private double currentRotation = 1.0;
	private int currentPhotoIndex = 0;
//	private boolean mouseMoved = false;
	private int lastMouseX = 0;
	
	double velocity = 10;
	RotationTimer timer = new RotationTimer();

	static final double ACCELERATION_DIFF = 0.002;
	static final double INITIAL_VELOCITY = 0.02;
	static final double VELOCITY_THRESHOLD = .00002;
	static final double MAX_VELOCITY = 0.004;
	
	double acceleration = .998;
	
	private Image backgroundImg;
	protected Dimension windowDimension;
	
    double containerWidth;
    double containerHeight;
    double boxHeight;
    double boxWidth;
    double xRadius;
    double yRadius;
    double spiralSpread;

    double logPosition;
    double logDistance;
    long logLastTime;
    double logVelocity;
    
    private int direction = 0;
    boolean debugMode = false;
    boolean keyDown = false;
    
    int lastKeycodePressed = -1;
        	
    
	protected Carousel(String backgroundImage, int size) {
		init(size);
		loadBackground(tracker, backgroundImage);
		
	}
	
	public Carousel(String backgroundImage, String[] imageNames) {
		init(imageNames.length);
        loadBackground(tracker, backgroundImage);
		loadImages(tracker, imageNames);
	}
	
	public Carousel(String backgroundImage, ArrayList<CarouselImage> images) {
		init(images.size());
        loadBackground(tracker, backgroundImage);
		loadImages(tracker, images);
	}

	protected void init(int size) {
		setBackground(Color.BLACK);
        setDoubleBuffered(true);
        this.images = new CarouselImage[size];
        this.addMouseMotionListener(this);
        
        this.tracker = new MediaTracker(this);
        
        this.addKeyListener(KeyListenerWrapper.init(this, true));

	}
	
	protected void loadBackground(MediaTracker trac, String backgroundImage) {
		tracker = new MediaTracker(this);
        backgroundImg = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(backgroundImage));
        trac.addImage(backgroundImg, 1);
	}
	

	protected void loadImages(MediaTracker trac, String[] imageNames) {
        for (int i=0;i<imageNames.length;i++) {
            CarouselImage ii = new CarouselImage(this.getClass().getResource(imageNames[i]));
            Image image = ii.getImage();       
            trac.addImage(image, 1);
            images[i] = ii;
        }		
	}
	
	protected void loadImages(MediaTracker trac,  ArrayList<CarouselImage> imageArray) {
        for (int i=0;i<imageArray.size();i++) {
            CarouselImage ii = imageArray.get(i);
            Image image = ii.getImage();       
            trac.addImage(image, 1);
            images[i] = ii;
        }		
	}
	
    public void addNotify() {
        super.addNotify();
        requestFocus();
        animator = new Thread(this);
        animator.start();
        rotater = new Thread(timer);
        rotater.start();
        
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        CarouselImage copy[] = Arrays.copyOf(this.images, this.images.length);
        try {
	        Arrays.sort(copy);
        }
        catch (Exception e) {
        	JukeboxFront.log.error(e);
        }
        
    	try {
	    	g.drawImage(backgroundImg, 0, 0, (int)windowDimension.getWidth(), (int)windowDimension.getHeight(), this);
	
			
			for (int i=0; i<copy.length; i++)
				copy[i].paint(g);
			
	        Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			
			if (debugMode) {
				g.setColor(Color.WHITE);
				g.drawString(String.format("currentIndex :: %s",  this.currentPhotoIndex), 20, 20);	
				g.drawString(String.format("currentRotation :: %s",  this.currentRotation), 20, 35);
				g.drawString(String.format("logPosition :: %s",  this.logPosition), 20, 50);
				g.drawString(String.format("logDistance :: %s",  this.logDistance), 20, 65);
				g.drawString(String.format("acceleration :: %s",  acceleration), 20, 80);
				g.drawString(String.format("velocity :: %s",  velocity), 20, 95);
				g.drawString(String.format("currentZIndex :: %s", images[this.currentPhotoIndex].getzIndex()), 20, 110);
				g.drawString(String.format("isKeyDown :: %s", this.keyDown), 20, 125);
				g.drawString(String.format("lastkey :: %s", this.lastKeycodePressed), 20, 140);
				g.drawString(String.format("timer enabled :: %s", timer.isEnabled()), 20, 155);
				g.drawString(String.format("logLastTime :: %s", logLastTime), 20, 170);
				g.drawString(String.format("logVelocity :: %s", logVelocity), 20, 185);				

			}
    	}
    	catch (Exception e) {
    		JukeboxFront.log.error("Error in paint method", e);
    		System.exit(-1);
		}
        Toolkit.getDefaultToolkit().sync();
    	
    }
    
    public void cycle() {
		placeImages();
    }

    
    private void initializeWindowSize() {
    	windowDimension = Toolkit.getDefaultToolkit().getScreenSize();
		
		// The size of the container the holds the images.
		containerWidth = windowDimension.getWidth();
		containerHeight = windowDimension.getHeight();

		// The base dimensions for each image. Images are scaled from these base
		// dimensions.
		boxHeight = containerHeight * 3.0 / 8.0;
		boxWidth = boxHeight * 1.2;
		
		// The radius of the ellipse that the images are set around.
		xRadius = (containerWidth - boxWidth) / 2.0;
		yRadius = boxHeight / 5.0;
		
		// A factor for achieving the spiral affect. The greater this value, the
		// more pronounced the spiral effect.
		spiralSpread = yRadius * .5;    	
    }

	private void placeImages() {
		if (windowDimension == null)
			initializeWindowSize();
		
		// The fraction that the images are offset from a whole number rotation.
		// This value will be between -0.5 and 0.5.
		double decimalOffset = currentRotation - Math.round(currentRotation);
		
		// The angle (in radians) that the images are offset from the base
		// positions. Base positions are 0*, 45*, 90*, 135*, etc. This value
		// will be between -22.5* and 22.5*.
		double angleOffset = -(decimalOffset * ((Math.PI) / 4));

		for (int i = 0; i < images.length; i++) {
			// The actual angle of the given image from the front.
			double angle = ((i * Math.PI) / 4) + angleOffset;
			
			// These are the simple x and y coordinates of the angel in a unit
			// circle. We flipped some of the signs and dimensions around
			// because our coordinate plane is a little turned around.
			double x = -Math.sin(angle);
			double y = -Math.cos(angle);
			
			// The factor by which to scale the image (i.e. make it smaller or
			// larger). This is based solely on the 'y' coordinate.
			double scale = Math.abs(Math.pow(2, y-1));
			images[i].setScale(scale);
			
			// set the zindex so that images in the front appear on top of
			// images behind.
			int zindex = (int) (y * 10) + 10;
			images[i].setzIndex(zindex);

			// set the size of the image. The aspect ratio of the image is
			// maintained as the image is scaled so that it fits inside the
			// correct "box" dimensions.
			
			// The x coordinate is obtained by simply scaling the unit-circle x
			// coordinate to fit the container.
			int xcoord = (int) Math.round((x * xRadius) + (containerWidth - images[i].getIconWidth()) / 2.0);
			images[i].setX(xcoord);
			
			// The y coordinate is similarly calculated, except that the spiral
			// factor is also added. Basically, the farther the image is around
			// the circle, the farther down it is shifted to give the spiral
			// effect.
			float spiralFactor = Math.round(spiralSpread * (i - 4 - decimalOffset));
			int ycoord = (int) Math.round((y * yRadius) + containerHeight - boxHeight - yRadius - images[i].getIconHeight()
					/ 2.0 - spiralFactor) - 200;
			images[i].setY(ycoord);
			
			// Finally, fade out the images that are at the very back. Make sure
			// the rest have full opacity.
			images[i].setAlpha(.5f - (float)decimalOffset);
			
			float alpha = (float)Math.pow(scale, 3.0d);
			
			if (scale > 0.3 && Math.abs(spiralFactor) > boxHeight * 1/3 )
				images[i].setVisible(false);
			else
				images[i].setVisible(true);
			
			images[i].setAlpha(alpha);

		}
	}    
    
	@Override
	public void run() {
		long beforeTime, timeDiff, sleep;
		try {
			tracker.waitForID(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return;
		}
		
        beforeTime = System.currentTimeMillis();

        while (true) {

            cycle();
            repaint();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;

            if (sleep < 0)
                sleep = 2;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }

            beforeTime = System.currentTimeMillis();
        }		
	}

	private class RotationTimer implements Runnable {
		private boolean enabled = false;
		
		public void run() {
			long timeDiff, sleep;
			long lastTime =  System.currentTimeMillis();
			logLastTime = lastTime;
			
			while (true) {
				long currentTime = System.currentTimeMillis();
				int ticks = (int) (currentTime - lastTime);
				
				if (this.isEnabled()) {
					if (acceleration == 1.0) {
						setRotation(currentRotation + ticks * velocity);
					} else {
						double newVelocity = velocity * Math.pow(acceleration, ticks);

						if (Math.abs(acceleration) > 1 && Math.abs(newVelocity) > MAX_VELOCITY)
							newVelocity = Math.signum(newVelocity) * MAX_VELOCITY;
						
						if (Math.abs(newVelocity) < VELOCITY_THRESHOLD) {
							//set target rotation if we are under threshold 
							setRotation(currentRotation + distanceFromStartingVelocity(velocity, acceleration, VELOCITY_THRESHOLD));
							setVelocity(0.0);
						} else {
							setRotation(currentRotation + distanceForXTicks(velocity, acceleration, ticks));
							setVelocity(newVelocity);
						}
					}
				}
				timeDiff = System.currentTimeMillis() - lastTime;
	            sleep = DELAY - timeDiff;

	            lastTime = currentTime;
	            
	            if (sleep < 0)
	                sleep = 2;
	            try {
	                Thread.sleep(sleep);
	            } catch (InterruptedException e) {
	                System.out.println("interrupted");
	            }
	            
				
	            
			}
		}
		
		public void enable() {
			this.setEnabled(true);
		}
		
		public void disable() {
			this.setEnabled(false);
		}

		public boolean isEnabled() {
			return enabled;
		}

		private void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
	
	/**
	 * Set the speed for the carousel to rotate.
	 */
	public void setVelocity(double velocity) {
		this.velocity = velocity;
		
		if (Math.abs(velocity) < VELOCITY_THRESHOLD) {
			if (timer.isEnabled()) 
				timer.disable();
			
			this.velocity = 0;
		} 
	}
	
	public double getVelocity() {
		return velocity;
	}

	/**
	 * The current rotational position of the carousel. Rotation is based on
	 * indices in the photo list. So if the 3rd photo in the list is in the
	 * front of the carousel, currentRotation will be 2.0 (indicies are 0
	 * based).
	 */
	public double getRotation() {
		return currentRotation;
	}

	/**
	 * The current rotational position of the carousel. Rotation is based on
	 * indices in the photo list. So if the 3rd photo in the list is in the
	 * front of the carousel, currentRotation will be 2.0 (indicies are 0
	 * based).
	 */
	public void setRotation(double value) {
//		int pi = getPhotoIndex();
		currentRotation = modulus(value, this.images.length);
		setCurrentPhotoIndex((int) Math.round(currentRotation));
//		if (pi != getPhotoIndex()) {
//			PhotoToFrontEvent event = new PhotoToFrontEvent();
//			event.setPhoto(photos.get(getPhotoIndex()));
//			event.setPhotoIndex(getPhotoIndex());
//			fireEvent(event);
//		}
		placeImages();
	}
	
	public int getPhotoIndex() {
		return currentPhotoIndex;
	}
	
	private void setCurrentPhotoIndex(int photoIndex) {
		logListener.log(String.format("Setting current photo index :: %s", photoIndex));
		
		int size = this.images.length;
		
		if (this.currentPhotoIndex == photoIndex)
			return;
		
		photoIndex = modulus(photoIndex, size);
		
		if (this.currentPhotoIndex == photoIndex) {
			return;
		} else {
			int shiftOffset = photoIndex - this.currentPhotoIndex;
			if (shiftOffset < -(size / 2)) {
				shiftOffset += size;
			} else if (shiftOffset > (size / 2)) {
				shiftOffset -= size;
			}
			if (shiftOffset > 0) {
				// Next
				// Creating temp array of images to hold shifted images
				CarouselImage[] temps = new CarouselImage[shiftOffset];
				for (int j = 0; j < temps.length; j++) {
					temps[j] = images[j];
				}
				for (int i = 0; i < images.length - (shiftOffset); i++) {
					images[i] = images[i + (shiftOffset)];
				}
				// update from large array
				for (int k = 0; k < temps.length; k++) {
					int pIndex = photoIndex - 4 + size - shiftOffset + k;
					pIndex = modulus(pIndex, size);
					images[k + images.length - shiftOffset] = temps[k];
//					temps[k].setUrl(photos.get(pIndex).getUrl());
				}
			} else if (shiftOffset < 0) {
				shiftOffset *= -1;
				// Prev
				CarouselImage[] temps = new CarouselImage[shiftOffset];
				for (int j = 0; j < temps.length; j++) {
					temps[j] = images[j + images.length - shiftOffset];
				}
				for (int i = images.length - 1; i >= shiftOffset; i--) {
					images[i] = images[i - shiftOffset];
				}
				// update from large array
				for (int k = 0; k < temps.length; k++) {
					int pIndex = photoIndex - 4 + k;
					pIndex = modulus(pIndex, size);
					images[k] = temps[k];
					//temps[k].setUrl(photos.get(pIndex).getUrl());
				}
			}
			
//			for (int i = 0; i < preLoadSize; i++) {
//				images[i].getElement().getStyle().setProperty("display", "none");
//				images[images.length - i - 1].getElement().getStyle().setProperty("display", "none");
//			}
			
//			for (int i = 0; i < carouselSize; i++) {
//				images[i + preLoadSize].getElement().getStyle().setProperty("display", "");
//			}
			
			this.currentPhotoIndex = photoIndex;
		}
	}
	

	
	private double distanceFromStartingVelocity(double velocity, double acceleration, double finalVelocity) {
		if (velocity < 0)
			finalVelocity = -finalVelocity;
		return (finalVelocity - velocity) / log2(acceleration);
	}
	
	public static double velocityForDistance(double distance, double acceleration, double finalVelocity) {
		if (distance < 0)
			finalVelocity = -finalVelocity;
		
		return finalVelocity - distance * log2(acceleration);
	}
	
	private double distanceForXTicks(double velocity, double acceleration, int ticks) {
		return velocity * (Math.pow(acceleration, ticks) - 1) / log2(acceleration);
	}
	
	private int modulus(int a, int b){
		if(a < 0) {
			a = a % b;
			if (a < 0)
				a += b;
			return a;
		} else if (a == 0) {
			return 0;
		}
		return a % b;
	}
	
	private double modulus (double a, int b){
		if (a == 0.0)
			return 0.0;
		
		a = a - b * (((int) a) / b);
		if (a < 0.0)
			a += b;
		
		return a;
	}
	
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		int pointX = me.getPoint().x;

		double newVelocity = ((double)lastMouseX - (double)pointX) / 3.0;
		if (lastMouseX > 0 )
			this.setVelocity(newVelocity / 50);
		
		lastMouseX = pointX;
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		logListener.log("KeyPressed");
		
		keyDown = true;
		
		
		direction = 0;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			setSpinner(1, true, e.getKeyCode());
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			setSpinner(-1, true, e.getKeyCode());
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
		else if (e.getKeyCode() == 0)
			setDebugMode(!debugMode);

		this.lastKeycodePressed = e.getKeyCode();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		keyDown = false;
		
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
			JukeboxFront.log.debug("KEY UP");
			setSpinner(-1 * direction, false, e.getKeyCode());
		}
		
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	/**
	 * Start an animated rotation to the given position. Position is based on
	 * indices in the photo list. So to rotate to the 3rd photo in the list,
	 * pass 2.0 (indicies are 0 based) as the position.
	 */
	public void rotateTo(double position) {
		logPosition = position;
		
		int size = this.images.length;
		double distance = modulus(position, size) - currentRotation;
		
		if (distance > size / 2)
			distance -= size;
		else if (distance < size / -2)
			distance += size;
		
		logDistance = distance;
		logVelocity = velocityForDistance(distance, 1 - ACCELERATION_DIFF, VELOCITY_THRESHOLD); 
				
		setVelocity(logVelocity);
		setAcceleration(1 - ACCELERATION_DIFF);
	}
		
	public void setSpinner(int dir, boolean keyDown, int currentKey) {
		direction = dir;
		
		if (keyDown) {
			setAcceleration(1 + ACCELERATION_DIFF);
		}
		else {
			setAcceleration(1 - ACCELERATION_DIFF);
			rotateTo(this.currentPhotoIndex - direction);
		}
		
		if (direction != 0 && lastKeycodePressed != currentKey) {
			setVelocity(direction * INITIAL_VELOCITY);
		}
		
		if (!timer.isEnabled())
			timer.enable();

	}
	
	private void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}
	
	public int getCurrentIndex() {
		return (this.currentPhotoIndex + 4) % this.images.length;
	}
	
	public void setCurrentIndex(int index) {		
		rotateTo(index - 4);
		
		if (!timer.isEnabled())
			timer.enable();

	}

	public void setLogListener(LogListener logListener) {
		this.logListener = logListener;
	}
	
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
	private static double log2(double a) {
		return Math.log10(a) / Math.log10(2);
	}
}
