import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.net.URL;


public class ImageCanvas extends Canvas {

	private static final long serialVersionUID = 3026901210874148150L;
	Image image;

	public ImageCanvas(String name) {
		image = Toolkit.getDefaultToolkit().getImage(name);
		initialize();
	}
  
    public ImageCanvas(URL url) {
	    image = Toolkit.getDefaultToolkit().getImage(url);
    }

    private void initialize() {
    	MediaTracker media = new MediaTracker(this);
    
	    media.addImage(image, 0);
	    try {
	      media.waitForID(0);  
	      }
	    catch (Exception e) {}	  
    }

    public ImageCanvas(ImageProducer imageProducer) {
    	image = createImage(imageProducer);
    }

    public void paint(Graphics g) {
    	g.drawImage(image, 0,0, this);
    }

}