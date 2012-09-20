package se.qxx.android.jukebox.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class Model {
	
	private Movie currentMovie;
	
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
	
	private Model() {
		_movies = new ArrayList<Movie>();
		_players = new ArrayList<String>();
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
		return currentMovie;
	}

	public void setCurrentMovie(Movie currentMovie) {
		this.currentMovie = currentMovie;
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

	
}
