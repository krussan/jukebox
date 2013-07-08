import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;



public class CarouselImage extends ImageIcon implements Comparable<CarouselImage> {
	private int x = 10;
	private int y = 10;
	private int zIndex = 1;
	private float alpha = 1f;
	private double scale = 1;
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public CarouselImage(URL imageUrl) {
		super(imageUrl);
		
	}
	
	public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		int scaleX = (int) (this.getIconWidth() * scale);
		int scaleY = (int) (this.getIconHeight() * scale);
        g2d.drawImage(this.getImage(), x, y, scaleX, scaleY, null);
        
	}

	@Override
	public int compareTo(CarouselImage o) {
		// TODO Auto-generated method stub
		return this.getzIndex() - o.getzIndex();
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
}
