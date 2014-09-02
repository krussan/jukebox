package se.qxx.jukebox.domain;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class Slice {
	private Movie movie;
	private Series serie;
	
	public Slice(Movie m) {
		this.setMovie(m);
	}
	
	public Slice(Series s) {
		this.setSerie(s);
	}

	public Movie getMovie() {
		return movie;
	}

	private void setMovie(Movie movie) {
		this.movie = movie;
	}

	public Series getSerie() {
		return serie;
	}

	private void setSerie(Series serie) {
		this.serie = serie;
	}
	
	public boolean isTvEpisode() {
		return this.serie != null;
	}


}
