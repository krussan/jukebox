package se.qxx.jukebox.front.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.front.JukeboxFront;

public class MovieFinder {
	public static Movie search(String key) {		
		return search(Model.get().getMovies(), key);
	}
	
	public static Movie search(List<Movie> list, String key) {
		int index = searchIndex(list, key);
		
		JukeboxFront.log.debug(String.format("Found index :: %s", index));
		return list.get(index);
	}
	
	public static int searchIndex(String key) {
		int index = searchIndex(Model.get().getMovies(), key);
	    
		JukeboxFront.log.debug(String.format("Found index :: %s", index));

		return index;
	}
	
	public static int searchIndex(List<Movie> list, String key) {
		List<String> titles = new ArrayList<String>();
		
		// go
		// aaa
		// ccc
		// ggg
		// ppp
		
		for (int i=0; i<list.size(); i++) {
			if (key.toLowerCase().compareTo(list.get(i).getTitle().toLowerCase()) <=0)
				return i;
		}
			
		return list.size() - 1;
	}	
}
