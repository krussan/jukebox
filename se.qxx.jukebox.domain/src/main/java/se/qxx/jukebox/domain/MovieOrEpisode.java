package se.qxx.jukebox.domain;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;

public class MovieOrEpisode {
	private Movie _movie;
	private Episode _episode;
	
	public MovieOrEpisode(Movie m) {
		this.setMovie(m);
	}
	
	public MovieOrEpisode(Episode e) {
		this.setEpisode(e);
	}

	public Movie getMovie() {
		return _movie;
	}

	public void setMovie(Movie _movie) {
		this._movie = _movie;
	}

	public Episode getEpisode() {
		return _episode;
	}

	public void setEpisode(Episode _series) {
		this._episode = _series;
	}
	
	public boolean isEpisode() {
		return this.getEpisode() != null;
	}
	
	public boolean isEmpty() {
		return this.getEpisode() == null && this.getMovie() == null;
	}

	public String getTitle() {
		if (this.isEpisode())
			return String.format("%s S%sE%s", 
					this.getSeries().getTitle(), 
					this.getSeries().getSeason(0).getSeasonNumber(),
					this.getEpisode().getEpisodeNumber());
		else
			return this.getMovie().getTitle();		
	}
}
