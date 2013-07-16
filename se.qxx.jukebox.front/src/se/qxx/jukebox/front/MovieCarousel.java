package se.qxx.jukebox.front;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class MovieCarousel extends Carousel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 311298267035861868L;

	public MovieCarousel(String backgroundImage, List<Movie> movies) {
		super(backgroundImage, movies.size());
		
		ArrayList<CarouselImage> images = new ArrayList<CarouselImage>();
		for(Movie m : movies) 
			images.add(new CarouselImage(m.getImage().toByteArray()));
		
		loadImages(tracker, images);
	}
	
}
