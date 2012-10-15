package se.qxx.android.jukebox.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public class Model {
	
	private int currentMovieId = -1;
	private int currentSubId = -1;
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
	private List<String> _players;
	private List<Subtitle> _subs;
	
	private Model() {
		_movies = new ArrayList<Movie>();
		_players = new ArrayList<String>();
		_subs = new ArrayList<Subtitle>();		
	}
	
	public static Model get() {
		if (_instance == null)
			_instance = new Model();
		
		return _instance;
	}
	
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
		if (this.currentMovieId == this._movies.size())
			this.currentMovieId = 0;
	}
	
	public void currentMovieSetPrevious() {
		this.currentMovieId--;
		if (this.currentMovieId < 0)
			this.currentMovieId = this._movies.size() == 0 ? 0 : this._movies.size() - 1;
	}
	
	public void setCurrentMovie(int index) {
		
		this.currentMovieId = index;
	}
	
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
	
	public void sortMovies() {
		Collections.sort(_movies, new Comparator<Movie>() {
			@Override
			public int compare(Movie lhs, Movie rhs) {
				
				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		});
	}

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

		return (String[])strings.toArray();
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

	public void setCurrentSubtitle(int id) {
		currentSubId = id;
	}

	public void setCurrentSubtitle(String description) {
		for (int i = 0;i<_subs.size(); i++) {
			Subtitle s = _subs.get(i);
			if (StringUtils.equalsIgnoreCase(description, s.getDescription()) || StringUtils.equalsIgnoreCase(description, s.getFilename())) {
				currentSubId = i;
				fireModelUpdatedEvent(ModelUpdatedType.CurrentSub);
				break;
			}				
		}
	}
	
	public int getCurrentSubtitleID() {
		return currentSubId;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	
}
