package se.qxx.jukebox.front;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.front.model.Model;

public class MovieCarousel extends Carousel {

	InfoBox info = new InfoBox();
	/**
	 * 
	 */
	private static final long serialVersionUID = 311298267035861868L;
    private MovieStatusListener listener;
    
	public MovieCarousel(String backgroundImage, List<Movie> movies) {
		super(backgroundImage, movies.size());
		
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
	}

}
