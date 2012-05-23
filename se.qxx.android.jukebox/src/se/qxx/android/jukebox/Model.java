package se.qxx.android.jukebox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class Model {
	
	private Movie currentMovie;
	
	public class ModelUpdatedEvent extends java.util.EventObject {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6575858181792885533L;

		public ModelUpdatedEvent(Object source) {
			super(source);
		}
	}
	
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
	
	private synchronized void fireModelUpdatedEvent() {
		ModelUpdatedEvent event = new ModelUpdatedEvent(this);
		Iterator<ModelUpdatedEventListener> i = _listeners.iterator();

		while(i.hasNext())  {
			i.next().handleModelUpdatedEventListener(event);
		}
	}

		
	private static Model _instance;
	private List<Movie> _movies;
	
	private Model() {
		_movies = new ArrayList<Movie>();
	}
	
	public static Model get() {
		if (_instance == null)
			_instance = new Model();
		
		return _instance;
	}
	
	public void addMovie(Movie movie) {
		_movies.add(movie);
		fireModelUpdatedEvent();
	}
	
	public void removeMovie(Movie movie) {
		_movies.remove(movie);
		fireModelUpdatedEvent();
	}
	
	public void addAllMovies(List<Movie> movies) {
		_movies.addAll(movies);
		fireModelUpdatedEvent();
	}
	
	public void clearMovies() {
		_movies.clear();
		fireModelUpdatedEvent();
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

	
}
