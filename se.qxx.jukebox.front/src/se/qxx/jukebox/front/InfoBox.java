package se.qxx.jukebox.front;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class InfoBox extends Canvas {

	private static final long serialVersionUID = -1946627803885458231L;
	private LinkedList<String> logs = new LinkedList<String>();
	
	private MovieOrSeries mos;
	private boolean debugMode = false;
	

	public InfoBox() {
	}

	public void addLog(String msg) {
		if (logs.size() > 0 && !StringUtils.equalsIgnoreCase(msg, logs.getLast()))
			logs.add(msg);
		
		cleanLog();
	}
	
	public MovieOrSeries getMovieOrSeries() {
		return mos;
	}
	public void setMovieOrSeries(MovieOrSeries mos) {
		this.mos = mos;
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
		
		float boxTop = (float)d.getHeight() - 170;
		boxTop = RenderUtils.setupWindow(g2, d, boxTop, 150);
        
		g2.setColor(new Color(240,240,240));
		g2.setFont(new Font("Calibri", Font.BOLD, 24));
		
		g.drawString(this.getMovieOrSeries().getMainTitle(), 30, (int)boxTop + 30);
		
		if (!this.getMovieOrSeries().isSeries()) {
			if (this.getMovieOrSeries().getMovie().getYear() > 0)
				RenderUtils.rightText(String.format("[%s]", this.getMovieOrSeries().getMovie().getYear()), (int)boxTop + 30, g, (int)d.getWidth(), 20);
		}


		g2.setFont(new Font("Calibri", Font.PLAIN, 16));
		
		int newY = RenderUtils.drawTextarea(g, this.getMovieOrSeries().getMainStory(), 30, (int)boxTop + 50, (int)(d.getWidth() * 4 / 5 - 30));
		newY += 10;
		
		if (!this.getMovieOrSeries().isSeries()) {
			g.drawString(String.format("Director :: %s", this.getMovieOrSeries().getMovie().getDirector()), 30, newY);
			g.drawString(String.format("Duration :: %s min", this.getMovieOrSeries().getMovie().getDuration()), 30, newY + 20);
		}
		
		String rating = this.getMovieOrSeries().getMainRating();
		if (!StringUtils.isEmpty(rating))
			RenderUtils.rightText(String.format("%s / 10", rating), (int)boxTop + 50, g, (int)d.getWidth(), 20);
//		rightText(String.format("%s / 10", this.getMovie().getRating()), (int)boxTop + 50, g, (int)d.getWidth(), 20);

		// Year, director, duration, rating, Group
//		drawString(g, this.getMovie().getYear(), d.getWidth() - , (int)boxTop + 50, (int)(d.getWidth() / 2 - 60));
		
//        centerText(
//        	String.format("Title :: %s", this.getMovie().getTitle()),
//        	(int)d.getHeight() - 100,
//        	g,
//        	(int)d.getWidth());

	}
	
//	private float setupWindow(Graphics2D g2, Dimension d) {
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		
//		Stroke oldStroke = g2.getStroke();
//		g2.setStroke(new BasicStroke(3.0f));
//		g2.setColor(new Color(224,172,27));
//
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
//
//        float boxTop = (float)d.getHeight() - 170;
//    	RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(10.0f, boxTop, (float)d.getWidth() - 20, 150, 20, 20);
//        g2.draw(roundedRectangle);
//        g2.setStroke(oldStroke);
//        
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
//        g2.fill(roundedRectangle);
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
//        
//        return boxTop;
//	}
	
	private void renderLogWindow(Graphics g) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Graphics2D g2 = (Graphics2D)g;
		float boxTop = (float)d.getHeight() - 170;
		boxTop = RenderUtils.setupWindow(g2, d, boxTop, 150);
		
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
}
