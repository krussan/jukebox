package se.qxx.jukebox.front;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;



public class CarouselImage extends ImageIcon implements Comparable<CarouselImage> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6441036011624653723L;
	private int x = 10;
	private int y = 10;
	private int zIndex = 1;
	private float alpha = 1f;
	private double scale = 1;
	private boolean visible = true;
	
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

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}	

	public CarouselImage(URL imageUrl) {
		super(imageUrl);
	}

	public CarouselImage(Image image) {
		super(image);
	}
	
	public CarouselImage(byte[] imageData) {
		super(imageData);
	}

	public void paint(Graphics g) {
		if (this.visible) {
	        Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			int scaleX = (int) (this.getIconWidth() * scale);
			int scaleY = (int) (this.getIconHeight() * scale);
	        g2d.drawImage(this.getImage(), x, y, scaleX, scaleY, null);
		}
	}

	@Override
	public int compareTo(CarouselImage o) {
		Integer i1 = new Integer(this.getzIndex());
		Integer i2 = new Integer(o.getzIndex());
		return i1.compareTo(i2);
	}

	@Override
	public boolean equals(Object o) {
		return compareTo((CarouselImage)o) == 0;
	}

}
