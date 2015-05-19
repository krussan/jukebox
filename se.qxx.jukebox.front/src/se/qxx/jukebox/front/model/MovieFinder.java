package se.qxx.jukebox.front.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
		//int index = searchIndex2(Model.get().getMovies(), key);
		int index = linearSearch(Model.get().getMovies(), key);
		
		if (index >= 0) {
			Movie m = Model.get().getMovie(index);
		
			JukeboxFront.log.debug(String.format("Found index :: %s - %s", index, m.getTitle()));
		}
		return index;
	}
	
	private static int searchIndex(List<Movie> list, String key) {
		// go
		// aaa
		// ccc 1 -> --
		// ggg0
		// ppp
		// zzz
		JukeboxFront.log.debug(String.format("Searching for %s", key));
		
		int mid = list.size() / 2;
		Movie m = list.get(mid);
		int result = key.toLowerCase().compareTo(m.getTitle().toLowerCase().trim());
		
		JukeboxFront.log.debug(String.format("Testing :: %s - %s", m.getTitle(), result));
		
		if (result == 0)
			return mid;
		
		if (list.size() == 1)
			return -1;
		
		if (result < 0) {
			result = searchIndex(list.subList(0, mid - 1), key);
		}
		else {
			result = mid + 1 + searchIndex(list.subList(mid + 1, list.size() - 1), key);
		}
		
		if (result == -1)
			return mid;
		else
			return result;
	}	
	
	private static int searchIndex2(List<Movie> list, String key) {
		if (list.size() == 0)
			return -1;
		else if (compare(list.get(list.size() - 1), key) > 0)
			return list.size() - 1;
		else {
			int low = 0;
			int high = list.size() - 1;
			int mid = -1;
			
			while(low<=high) {
				mid = (low + high) / 2;
				Movie m = list.get(mid);
				int result = compare(m, key);
				
				JukeboxFront.log.debug(String.format("Testing %s - %s :: %s", mid, m.getTitle(), result));
				
				if (result < 0) 
					high = mid - 1;
				else if (result > 0)
					low = mid + 1;
				else {
					return mid;
				}
			}
			
			return mid;
		}
	}
	
	private static int linearSearch(List<Movie> list, String key) {
		for (int i=0;i<list.size(); i++) {
			if (StringUtils.startsWithIgnoreCase(list.get(i).getTitle(), key))
				return i;
		}
		
		return -1;
	}
	
	private static int compare(Movie movie, String key) {
		return key.toLowerCase().compareTo(movie.getTitle().toLowerCase().trim());
	}
}
