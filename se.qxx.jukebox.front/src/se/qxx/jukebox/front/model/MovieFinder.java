package se.qxx.jukebox.front.model;

import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class MovieFinder {
	public static Movie search(List<Movie> list, String key) {
		return search(list, key, 0, list.size() - 1);
	}
	
	private static Movie search(List<Movie> A, String key, int imin, int imax)
	{
	  // test if array is empty
	  if (imax < imin)
	    // set is empty, so return value showing not found
	    return null;
	  else
	    {
	      // calculate midpoint to cut set in half
	      int imid = (imax - imin) / 2;
	 
	      // three-way comparison
	      Movie mid = A.get(imid);
	      Movie child = mid;
	      
	      int comparison = compare(mid, key);
	      if (comparison > 0)
	        // key is in lower subset
	        child = search(A, key, imin, imid - 1);
	      else if (comparison < 0)
	        // key is in upper subset
	        child = search(A, key, imid + 1, imax);
	      
	      // key has not been found in child search.
	      // return the closest there is	      
	      if (child == null)
	        child = mid;
	      
	      return child; 
	    }
	}
	
	private static int compare(Movie m, String key) {
		return m.getTitle().compareTo(key);
	}
}