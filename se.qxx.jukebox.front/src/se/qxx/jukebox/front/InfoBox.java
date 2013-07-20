package se.qxx.jukebox.front;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class InfoBox extends Canvas {

	private static final long serialVersionUID = -1946627803885458231L;
	
	private Movie movie;
	
	public InfoBox() {
	}
	
	public Movie getMovie() {
		return movie;
	}
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(3.0f));
		g2.setColor(new Color(224,172,27));

    	RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(10.0f, (float)d.getHeight() - 170, (float)d.getWidth() - 20, 150, 20, 20);
        g2.draw(roundedRectangle);
        g2.setStroke(oldStroke);
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2.fill(roundedRectangle);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
    	String s = String.format("Title :: %s", this.getMovie().getTitle());
    	int stringLen = (int)g.getFontMetrics().getStringBounds(s, g).getWidth();  
        int x = (int)d.getWidth() / 2 - stringLen/2;
        int y = (int)d.getHeight() - 100;
        g.setColor(Color.WHITE);
        g.drawString(s, x, y);        
	}



}
