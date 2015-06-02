package se.qxx.jukebox.front;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.front.input.T9;
import se.qxx.jukebox.front.input.T9.KeyInputCompletedListener;
import se.qxx.jukebox.front.input.T9InputCompletedEvent;
import se.qxx.jukebox.front.model.Model;
import se.qxx.jukebox.front.model.MovieFinder;

public class MovieCarousel extends Carousel implements LogListener, KeyInputCompletedListener {

	boolean debugMode = false;
	
	InfoBox info = new InfoBox();
	InputBox searchInputBox = new InputBox();
	T9 teeniner = new T9();


	public enum DisplayType {
		Movie
	 ,  Series
	}
	
	private DisplayType currentType = DisplayType.Movie;

	/**
	 * 
	 */
	private static final long serialVersionUID = 311298267035861868L;
    private MovieStatusListener listener;
    
	public MovieCarousel(String backgroundImage) {
		super(backgroundImage, Model.get().getMovies().size());
		
		super.setLogListener(this);
		
		loadImages(tracker, getMovieImageSet());
		
		teeniner.addEventListener(this);
	}
	
	private ArrayList<CarouselImage> getMovieImageSet() {
		ArrayList<CarouselImage> images = new ArrayList<CarouselImage>();
		for(Movie m : Model.get().getMovies()) {
			if (m.getImage().isEmpty())
				images.add(new CarouselImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/movie.png"))));
			else
				images.add(new CarouselImage(m.getImage().toByteArray()));
		}
		
		return images;
	}
	

	private ArrayList<CarouselImage> getSeriesImageSet() {
		ArrayList<CarouselImage> images = new ArrayList<CarouselImage>();
		for(Series s : Model.get().getSeries()) {
			if (s.getImage().isEmpty())
				images.add(new CarouselImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/movie.png"))));
			else
				images.add(new CarouselImage(s.getImage().toByteArray()));
		}
		
		return images;
	}
	
    public void paint(Graphics g) {
    	super.paint(g);
    	
    	// paint information about movie.
    	MovieOrSeries mos = getCurrentMovieOrSeries();    	
    	  
    	info.setMovieOrSeries(mos);
    	info.paint(g);

    	searchInputBox.paint(g);
    }
    
	private MovieOrSeries getCurrentMovieOrSeries() {
    	if (this.currentType == DisplayType.Movie) 
    		return new MovieOrSeries(Model.get().getMovie(super.getCurrentIndex()));
    	else
    		return new MovieOrSeries(Model.get().getSeries(super.getCurrentIndex()));
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
		else if (e.getKeyCode() == KeyEvent.VK_UP)
			moveUpwards();
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			moveDownwards();
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


	@Override
	public void handleKeyInputCompletedListener(EventObject e) {
		
		T9InputCompletedEvent event = (T9InputCompletedEvent)e;
		JukeboxFront.log.debug(String.format("Input completed :: %s", event.getInput()));
		
		int newIndex = MovieFinder.searchIndex(event.getInput());
		JukeboxFront.log.debug(String.format("New Index :: %s", newIndex));
	
		if (newIndex >= 0)
			super.setCurrentIndex(newIndex);
	}


	@Override
	protected ArrayList<CarouselImage> nextImageSet(int direction) {
		if (currentType == DisplayType.Movie){
			currentType = DisplayType.Series;
			return getSeriesImageSet();
		}
		else {
			currentType = DisplayType.Movie;
			return getMovieImageSet();
		}
		
		
		
	}

}
