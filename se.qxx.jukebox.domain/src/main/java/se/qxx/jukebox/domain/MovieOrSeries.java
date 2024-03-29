package se.qxx.jukebox.domain;
import java.util.List;

import com.google.protobuf.ByteString;
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
	
	public void setSeasonAndEpisode(int season, int episode) {
		if (this.isSeries()) {	
			Series s = this.getSeries();
			Season sn = s.getSeason(0);
			Episode ep = sn.getEpisode(0); 
			
			ep = Episode.newBuilder(ep)
					.setEpisodeNumber(episode)
					.build();
			
			sn = Season.newBuilder(sn)
					.setSeasonNumber(season)
					.build();
			
			sn = DomainUtil.updateEpisode(sn, ep);
			s = DomainUtil.updateSeason(s, sn);
			
			this.setSeries(s);
		}	
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
	
	public List<Media> getMediaList() {
		if (this.isSeries())
			return this.getEpisode().getMediaList();
		else
			return this.getMovie().getMediaList();				
	}

	public ByteString getMediaCount() {
		if (this.isSeries())
			return this.getSeries().getThumbnail();
		else
			return this.getMovie().getThumbnail();
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
	
	public String getMainTitle() {
		if (this.isSeries())
			return this.getSeries().getTitle();
		else
			return this.getMovie().getTitle();
		
	}

	public String getTitle() {
		if (this.isSeries())
			return String.format("%s S%sE%s", 
					this.getSeries().getTitle().trim(), 
					MovieOrSeries.padLeft(Integer.toString(this.getSeries().getSeason(0).getSeasonNumber()), 2),
					MovieOrSeries.padLeft(Integer.toString(this.getEpisode().getEpisodeNumber()), 2));
		else
			return this.getMovie().getTitle();		
	}

	public int getYear() {
		if (this.isSeries())
			return this.getSeries().getYear();
		else
			return this.getMovie().getYear();
	}

	public String getRating() {
		if (this.isSeries())
			return this.getSeries().getRating();
		else
			return this.getMovie().getRating();
	}

	public ByteString getThumbnail() {
		if (this.isSeries())
			return this.getSeries().getThumbnail();
		else
			return this.getMovie().getThumbnail();
	}

	public String getGroupName() { 
		if (this.isSeries())
			return this.getEpisode().getGroupName();
		else
			return this.getMovie().getGroupName();
	}
	
	public String getFormat() { 
		if (this.isSeries())
			return this.getEpisode().getFormat();
		else
			return this.getMovie().getFormat();
	}
	
	public Episode getEpisode() {
		return this.getSeries().getSeason(0).getEpisode(0);
	}

	public Season getSeason() {
		return this.getSeries().getSeason(0);
	}

	public String getMainStory() {
		if (this.isSeries())
			return this.getSeries().getStory();
		else
			return this.getMovie().getStory();
	}

	public String getMainRating() {
		if (this.isSeries())
			return this.getSeries().getRating();
		else
			return this.getMovie().getRating();
	}
	
	public int getID() {
		if (this.isSeries())
			return this.getEpisode().getID();
		else
			return this.getMovie().getID();
	}
	
	public static String padLeft(String text, int totalLength) {
		if (totalLength <= text.length())
			return text;
		
		String result = ("0000000000".substring(text.length()) + text);
		return result.substring(result.length() - totalLength, result.length());
	}

}
