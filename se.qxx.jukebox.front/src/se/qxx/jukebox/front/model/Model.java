package se.qxx.jukebox.front.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public class Model {
	
	private int currentMovieId = -1;
	private String currentSub = StringUtils.EMPTY;
	private int currentMediaId = -1; 
	private boolean initialized = false;
	
	public interface ModelUpdatedEventListener {
		public void handleModelUpdatedEventListener(java.util.EventObject e);
	}

	private List<ModelUpdatedEventListener> _listeners = new ArrayList<Model.ModelUpdatedEventListener>();
	
	public synchronized void addEventListener(ModelUpdatedEventListener listener) {
		_listeners.add(listener);
	}
	
	public synchronized void removeEventListener(ModelUpdatedEventListener listener) {
		_listeners.remove(listener);
	}
	
	private synchronized void fireModelUpdatedEvent(ModelUpdatedType type) {
		ModelUpdatedEvent event = new ModelUpdatedEvent(this, type);
		Iterator<ModelUpdatedEventListener> i = _listeners.iterator();

		while(i.hasNext())  {
			i.next().handleModelUpdatedEventListener(event);
		}
	}
		
	private static Model _instance;
	private List<Movie> _movies;
	private List<Series> _series;
	private List<String> _players;
	private List<Subtitle> _subs;
	
	private Model() {
		_movies = new ArrayList<Movie>();
		_series = new ArrayList<Series>();
		_players = new ArrayList<String>();
		_subs = new ArrayList<Subtitle>();
	}
	
	public static Model get() {
		if (_instance == null)
			_instance = new Model();
		
		return _instance;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	//---------------------------------------------------------------------------------------
	// MOVIE
	//---------------------------------------------------------------------------------------

	public void addMovie(Movie movie) {
		_movies.add(movie);
		fireModelUpdatedEvent(ModelUpdatedType.Movies);
	}
	
	public void removeMovie(Movie movie) {
		_movies.remove(movie);
		fireModelUpdatedEvent(ModelUpdatedType.Movies);
	}
	
	public void addAllMovies(List<Movie> movies) {
		_movies.addAll(movies);
		sortMovies(); 
		fireModelUpdatedEvent(ModelUpdatedType.Movies);
	}
	
	public void clearMovies() {
		_movies.clear();
		fireModelUpdatedEvent(ModelUpdatedType.Movies);
	}
	
	public Movie getMovie(int position) {
		return _movies.get(position);
	}
	
	public List<Movie> getMovies() {
		return _movies;
	}
	
	public int countMovies() {
		return _movies.size();
	}

	public Movie getCurrentMovie() {
		if (this.currentMovieId >= 0 && this.currentMovieId < this._movies.size())
			return this._movies.get(this.currentMovieId);
		else
			return null;
	}

	public int getCurrentMovieIndex() {
		return this.currentMovieId;
	}

	public Movie getPreviousMovie() {
		if (this.currentMovieId == 0)
			return this._movies.get(this._movies.size() == 0 ? 0 : this._movies.size() - 1);
		else
			return this._movies.get(this.currentMovieId - 1);
	}
	
	public Movie getNextMovie() {
		if (this.currentMovieId == this._movies.size() - 1)
			return this._movies.get(0);
		else
			return this._movies.get(this.currentMovieId + 1);
	}	
	
	public void currentMovieSetNext() {
		this.currentMovieId++;
		this.currentMediaId = 0;
		if (this.currentMovieId == this._movies.size())
			this.currentMovieId = 0;
	}
	
	public void currentMovieSetPrevious() {
		this.currentMovieId--;
		this.currentMediaId = 0;
		if (this.currentMovieId < 0)
			this.currentMovieId = this._movies.size() == 0 ? 0 : this._movies.size() - 1;
	}
	
	public void setCurrentMovie(int index) {
		this.currentMovieId = index;
		this.currentMediaId = 0;		
	}

	public void sortMovies() {
		Collections.sort(_movies, new MovieComparator());
	}
	
	public Movie findMovie(String searchstring) {
		return MovieFinder.search(_movies, searchstring);
	}

	private class MovieComparator implements Comparator<Movie> {

		@Override
		public int compare(Movie lhs, Movie rhs) {
			return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
		}
		
	}
	
	private class SeriesComparator implements Comparator<Series> {

		@Override
		public int compare(Series o1, Series o2) {
			return o1.getTitle().compareToIgnoreCase(o2.getTitle());
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	// SERIES
	//---------------------------------------------------------------------------------------
		
	public void addAllSeries(List<Series> series) {
		_series.addAll(series);
		Collections.sort(_series, new SeriesComparator());
		
		fireModelUpdatedEvent(ModelUpdatedType.Movies);
	}
	
	public void clearSeries() {
		_series.clear();
		fireModelUpdatedEvent(ModelUpdatedType.Movies);
	}
	
	public List<Series> getSeries() {
		return _series;
	}
	

	public Series getSeries(int position) {
		return _series.get(position);
	}

	//---------------------------------------------------------------------------------------
	// MEDIA
	//---------------------------------------------------------------------------------------
	
	public Media getCurrentMedia() {
		Movie m = getCurrentMovie();
		if (m != null) {
			if (this.currentMediaId >= 0 && this.currentMediaId < m.getMediaList().size())
				return m.getMediaList().get(this.currentMediaId);
			else
				return null;			
		}
		else {
			return null;
		}
	}

	public Media getPreviousMedia() {
		Movie m = getCurrentMovie();
		if (m != null) {
			int size = m.getMediaList().size();
			if (size == 0)
				return null;
			else {
				if (this.currentMediaId == 0)
					return m.getMedia(size - 1);
				else
					return m.getMedia(this.currentMediaId - 1);
			}
		}
		else
			return null;
	}
	
	public Media getNextMedia() {
		Movie m = getCurrentMovie();
		if (m!=null) {			
			int size = m.getMediaList().size();
			if (size == 0)
				return null;
			else {
				if (this.currentMediaId == size - 1)
					return m.getMedia(0);
				else
					return m.getMedia(this.currentMediaId + 1);
			}
		}
		else {
			return null;
		}
	}	
	
	public void currentMediaSetNext() {
		this.currentMediaId++;
		Movie m = getCurrentMovie();
		if (m != null) {
			if (this.currentMediaId == m.getMediaList().size())
				this.currentMediaId = 0;
		}
	}
	
	public void currentMediaSetPrevious() {
		this.currentMediaId--;
		Movie m = getCurrentMovie();
		if (m != null) {
			int size = m.getMediaList().size();
			if (this.currentMediaId < 0)
				this.currentMediaId = size == 0 ? 0 : size - 1;
		}
	}
	
	public void setCurrentMedia(int index) {
		this.currentMediaId = index;
	}
	
	public void setCurrentMedia(Media media) {
		List<Media> listMedia = this.getCurrentMovie().getMediaList();
		
		for(int i=0;i<listMedia.size();i++) {
			if (listMedia.get(i).getID() == media.getID()) {
				this.setCurrentMedia(i);
				break;
			}
		}
	}
	
	public void setCurrentMedia(String filename) {
		List<Media> listMedia = this.getCurrentMovie().getMediaList();
		
		for(int i=0;i<listMedia.size();i++) {
			if (StringUtils.equalsIgnoreCase(listMedia.get(i).getFilename(), filename)) {
				this.setCurrentMedia(i);
				break;
			}
		}		
	}
	
	//---------------------------------------------------------------------------------------
	// PLAYERS
	//---------------------------------------------------------------------------------------
	
	public List<String> getPlayers() {
		return _players;
	}
	
	public void addAllPlayers(List<String> players) {
		_players.addAll(players);
		Collections.sort(_players);
		
		fireModelUpdatedEvent(ModelUpdatedType.Players);
	}
	
	public void clearPlayers() {
		_players.clear();
		fireModelUpdatedEvent(ModelUpdatedType.Players);
	}
	
	public int countPlayers() {
		return _players.size();
	}
	
	public String getPlayer(int index) {
		return _players.get(index);
	}
	
	//---------------------------------------------------------------------------------------
	// SUBTITLES
	//---------------------------------------------------------------------------------------
	
	public List<Subtitle> getSubtitles() {
		return _subs;
	}
	
	public String[] getSubtitleDescriptions() {
		List<String> strings = new ArrayList<String>();
		for (Subtitle s : this._subs) {
			String desc = s.getDescription();
			
			if (StringUtils.isEmpty(desc))
				strings.add(s.getFilename());
			else
				strings.add(desc);
		}

		return  (String[])strings.toArray(new String[0]);
	}
	
	public void addAllSubtitles(List<Subtitle> subs) {
		_subs.addAll(subs);
		
		fireModelUpdatedEvent(ModelUpdatedType.Subs);
	}
	
	public void clearSubtitles() {
		_subs.clear();
		fireModelUpdatedEvent(ModelUpdatedType.Subs);
	}
	
	public int countSubtitles() {
		return _subs.size();
	}
	
	public Subtitle getSubtitle(int index) {
		return _subs.get(index);
	}

//	public void setCurrentSubtitle(int id) {
//		currentSubId = id;
//		fireModelUpdatedEvent(ModelUpdatedType.CurrentSub);		
//	}

	public void setCurrentSubtitle(String description) {
		this.currentSub = description;
	}
	
	public String getCurrentSubtitle() {
		return this.currentSub;
	}
	
}
