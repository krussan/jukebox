package se.qxx.jukebox.domain;

import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class MovieOrSeries {
	private Movie _movie;
	private Series _series;
	
	public MovieOrSeries(Movie m) {
		this.setMovie(m);
	}
	
	public MovieOrSeries(Series s) {
		this.setSeries(s);
	}

	public Movie getMovie() {
		return _movie;
	}

	private void setMovie(Movie movie) {
		this._movie = movie;
	}

	public Series getSeries() {
		return _series;
	}

	private void setSeries(Series series) {
		this._series = series;
	}
	
	public boolean isSeries() {
		return this.getSeries() != null;
	}
	
	public boolean isEmpty() {
		return this.getSeries() == null && this.getMovie() == null;
	}
	
	public int getIdentifierRating() {
		if (this.isSeries())
			return this.getEpisode().getIdentifierRating();
		else
			return this.getMovie().getIdentifierRating();
	}
	
	public void setIdentifierRating(int rating) {
		if (this.isSeries()) {	
			Series s = this.getSeries();
			Season sn = s.getSeason(0);
			Episode ep = sn.getEpisode(0); 
			
			ep = Episode.newBuilder(ep)
					.setIdentifierRating(rating)
					.build();
			
			sn = DomainUtil.updateEpisode(sn, ep);
			s = DomainUtil.updateSeason(s, sn);
			
			this.setSeries(s);
		}
		else {
			// weigh the identifier
			Movie m = this.getMovie();
			this.setMovie(Movie.newBuilder(m)
					.setIdentifierRating(rating)
					.build());
		}	
	}
	
	public Identifier getIdentifier() {
		if (this.isSeries())
			return this.getEpisode().getIdentifier();
		else
			return this.getMovie().getIdentifier();
	}
	
	public void setIdentifier(Identifier identifier) {
		if (this.isSeries()) {	
			Series s = this.getSeries();
			Season sn = s.getSeason(0);
			Episode ep = sn.getEpisode(0); 
			
			ep = Episode.newBuilder(ep)
					.setIdentifier(identifier)
					.build();
			
			sn = DomainUtil.updateEpisode(sn, ep);
			s = DomainUtil.updateSeason(s, sn);
			
			this.setSeries(s);
		}
		else {
			// weigh the identifier
			Movie m = this.getMovie();
			this.setMovie(Movie.newBuilder(m)
					.setIdentifier(identifier)
					.build());
		}	
	}

	public Media getMedia() {
		if (this.isSeries())
			return this.getEpisode().getMedia(0);
		else
			return this.getMovie().getMedia(0);		
	}
	
	public void replaceMedia(Media md) {
		if (this.isSeries()) {	
			Series s = this.getSeries();
			Season sn = s.getSeason(0);
			Episode ep = sn.getEpisode(0); 
			
			ep = Episode.newBuilder(ep)
					.clearMedia()
					.addMedia(md)
					.build();
			
			sn = DomainUtil.updateEpisode(sn, ep);
			s = DomainUtil.updateSeason(s, sn);
			
			this.setSeries(s);
		}
		else {
			// weigh the identifier
			Movie m = this.getMovie();
			this.setMovie(Movie.newBuilder(m)
					.clearMedia()
					.addMedia(md)
					.build());
		}		
	}

	public String getTitle() {
		if (this.isSeries())
			return String.format("%s S%sE%s", 
					this.getSeries().getTitle(), 
					this.getSeries().getSeason(0).getSeasonNumber(),
					this.getEpisode().getEpisodeNumber());
		else
			return this.getMovie().getTitle();		
	}

	public String getGroupName() { 
		if (this.isSeries())
			return this.getEpisode().getGroupName();
		else
			return this.getMovie().getTitle();
	}
	
	public String getFormat() { 
		if (this.isSeries())
			return this.getEpisode().getFormat();
		else
			return this.getMovie().getTitle();
	}
	
	private Episode getEpisode() {
		return this.getSeries().getSeason(0).getEpisode(0);
	}
}
