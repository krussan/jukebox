package se.qxx.jukebox.front;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class BackDrop extends JPanel implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5859498040090776572L;
	private Image star;
	private Thread animator;
	private int x, y;
	private int directionX = 1;
	private int directionY = 1;
	
	private final int DELAY = 2;
	
	MediaTracker tracker;
	
	public BackDrop() {
		setBackground(Color.BLACK);
        setDoubleBuffered(true);
        tracker = new MediaTracker(this);
        
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/res/network.png"));
        star = ii.getImage();
    	
        tracker.addImage(star, 1);
        
        x = y = 10;	
	}
	
    public void addNotify() {
        super.addNotify();
        animator = new Thread(this);
        animator.start();
    }
    
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(star, x, y, this);
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }
    
    public void cycle() {
    	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        if (y >= d.getHeight() - star.getHeight(this) || y <= 0)
    		directionY = -1 * directionY;
        
        if (x >= d.getWidth() - star.getWidth(this) || x <= 0)
        	directionX = -1 * directionX;

        
    		
        x += directionX;
        y += directionY;

    }
    
	@Override
	public void run() {
		long beforeTime, timeDiff, sleep;
		try {
			tracker.waitForID(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
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

}
