package se.qxx.jukebox.front;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class InfoBox extends Canvas {

	private static final long serialVersionUID = -1946627803885458231L;
	private LinkedList<String> logs = new LinkedList<String>();
	
	private Movie movie;
	private boolean debugMode = false;
	

	public InfoBox() {
	}

	public void addLog(String msg) {
		if (logs.size() > 0 && !StringUtils.equalsIgnoreCase(msg, logs.getLast()))
			logs.add(msg);
		
		cleanLog();
	}
	
	public Movie getMovie() {
		return movie;
	}
	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}


	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (debugMode)
			renderLogWindow(g);
		else
			renderInfoWindow(g);
	}
	
	private void renderInfoWindow(Graphics g) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Graphics2D g2 = (Graphics2D)g;
		
		float boxTop = setupWindow(g2, d);
        
		g2.setColor(new Color(240,240,240));
		g2.setFont(new Font("Calibri", Font.BOLD, 24));
		g.drawString(this.getMovie().getTitle(), 30, (int)boxTop + 30);
		if (this.getMovie().getYear() > 0)
			rightText(String.format("[%s]", this.getMovie().getYear()), (int)boxTop + 30, g, (int)d.getWidth(), 20);


		g2.setFont(new Font("Calibri", Font.PLAIN, 16));		
		int newY = drawTextarea(g, this.getMovie().getStory(), 30, (int)boxTop + 50, (int)(d.getWidth() * 4 / 5 - 30));
		newY += 10;
		g.drawString(String.format("Director :: %s", this.getMovie().getDirector()), 30, newY);
		g.drawString(String.format("Duration :: %s min", this.getMovie().getDuration()), 30, newY + 20);
		
		String rating = this.getMovie().getRating();
		if (!StringUtils.isEmpty(rating))
			rightText(String.format("%s / 10", rating), (int)boxTop + 50, g, (int)d.getWidth(), 20);
//		rightText(String.format("%s / 10", this.getMovie().getRating()), (int)boxTop + 50, g, (int)d.getWidth(), 20);

		// Year, director, duration, rating, Group
//		drawString(g, this.getMovie().getYear(), d.getWidth() - , (int)boxTop + 50, (int)(d.getWidth() / 2 - 60));
		
//        centerText(
//        	String.format("Title :: %s", this.getMovie().getTitle()),
//        	(int)d.getHeight() - 100,
//        	g,
//        	(int)d.getWidth());

	}
	
	private float setupWindow(Graphics2D g2, Dimension d) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(3.0f));
		g2.setColor(new Color(224,172,27));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        float boxTop = (float)d.getHeight() - 170;
    	RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(10.0f, boxTop, (float)d.getWidth() - 20, 150, 20, 20);
        g2.draw(roundedRectangle);
        g2.setStroke(oldStroke);
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2.fill(roundedRectangle);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        return boxTop;
	}
	
	private void renderLogWindow(Graphics g) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Graphics2D g2 = (Graphics2D)g;
		float boxTop = setupWindow(g2, d);
		
		g2.setFont(new Font("Calibri", Font.PLAIN, 12));		
		
		int newY = (int)boxTop + 30;

		cleanLog();
			
		int retainer = logs.size() >= 8 ? 8 : logs.size();
		for (int i=logs.size() - retainer;i < logs.size();i++) {
			g.drawString(logs.get(i), 30, newY);
			newY += 15;
		}
	}
	
	private void cleanLog() {
		if (logs.size() >= 8) {
			for (int i=0;i<8;i++) {
				logs.remove();
			}
		}
	}
	
	private void rightText(String text, int yCoord, Graphics g, int containerWidth, int margin) {
    	int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();
    	int x = containerWidth - stringLen - margin;
    	
    	g.drawString(text, x, yCoord);
	}
	
	@SuppressWarnings("unused")
	private void centerText(String text, int yCoord, Graphics g, int containerWidth) {
    	int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();  
        int x = containerWidth / 2 - stringLen/2;
        
        g.drawString(text, x, yCoord);  
	}

	public int drawTextarea(Graphics g, String s, int x, int y, int width)
	{
		// FontMetrics gives us information about the width,
		// height, etc. of the current Graphics object's Font.
		FontMetrics fm = g.getFontMetrics();

		int lineHeight = fm.getHeight();

		int curX = x;
		int curY = y;

		String[] words = s.split(" ");

		for (String word : words)
		{
			// Find out thw width of the word.
			int wordWidth = fm.stringWidth(word + " ");

			// If text exceeds the width, then move to next line.
			if (curX + wordWidth >= x + width)
			{
				curY += lineHeight;
				curX = x;
			}
			
			g.drawString(word, curX, curY);

			// Move over to the right for next word.
			curX += wordWidth;
		}
		
		return curY + lineHeight;
	}
}
