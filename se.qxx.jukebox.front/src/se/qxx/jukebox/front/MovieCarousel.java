package se.qxx.jukebox.front;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.front.input.T9;
import se.qxx.jukebox.front.model.Model;

public class MovieCarousel extends Carousel implements LogListener {

	boolean debugMode = false;
	
	InfoBox info = new InfoBox();
	InputBox searchInputBox = new InputBox();
	T9 teeniner = new T9();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 311298267035861868L;
    private MovieStatusListener listener;
    
	public MovieCarousel(String backgroundImage, List<Movie> movies) {
		super(backgroundImage, movies.size());
		
		super.setLogListener(this);
		
		ArrayList<CarouselImage> images = new ArrayList<CarouselImage>();
		for(Movie m : movies) {
			if (m.getImage().isEmpty())
				images.add(new CarouselImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/movie.png"))));
			else
				images.add(new CarouselImage(m.getImage().toByteArray()));
		}
		
		loadImages(tracker, images);
	}
	

    public void paint(Graphics g) {
    	super.paint(g);
    	
    	// paint information about movie.
    	Movie m = Model.get().getMovie(super.getCurrentIndex());
    	  
    	info.setMovie(m);
    	info.paint(g);

    	searchInputBox.paint(g);
    }
    
	public void setMovieStatusListener(MovieStatusListener movieStatusListener) {
		this.listener = movieStatusListener;
	}
	
	public MovieStatusListener getMovieStatusListener(MovieStatusListener movieStatusListener) {
		return this.listener;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		if (e.getKeyCode() == 0) {
			this.debugMode = !this.debugMode;
			super.setDebugMode(this.debugMode);
			info.setDebugMode(this.debugMode);
		}
		else if (e.getKeyCode() >= 0x30 && e.getKeyCode() <= 0x39)
			addToSearchString(e.getKeyCode() - 0x30);
		else if (e.getKeyCode() >= 0x60 && e.getKeyCode() <= 0x69)
			addToSearchString(e.getKeyCode() - 0x60);
		else if (e.getKeyCode() == 8)
			clearSearchString();
	}
	
	public void clearSearchString() {
		teeniner.setTypedInput(StringUtils.EMPTY);
		searchInputBox.setSearchString(StringUtils.EMPTY);
	}

	public void addToSearchString(int keyNumber) {
		teeniner.addKey(keyNumber);
		searchInputBox.setSearchString(teeniner.getTypedInput());
	}

	@Override
	public void log(String message) {
		info.addLog(message);
	}

}
