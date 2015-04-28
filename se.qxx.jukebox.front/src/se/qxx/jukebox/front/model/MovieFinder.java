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
		// go
		// aaa
		// ccc
		// ggg
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
			result = searchIndex(list.subList(mid + 1, list.size() - 1), key);
		}
		
		if (result == -1)
			return mid;
		else
			return result;
		
		
//		for (int i=0; i<list.size(); i++) {
//			int result = key.toLowerCase().compareTo(list.get(i).getTitle().toLowerCase().trim());
//			JukeboxFront.log.debug(String.format("Testing :: %s - %s", list.get(i).getTitle(), result));
//			
//			if (result <= 0) {
//				JukeboxFront.log.debug("--- FOUND ---");
//				return i;
//			}
//		}
//			
//		return list.size() - 1;
	}	
}
