package se.qxx.jukebox.builders;

import java.util.Comparator;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class MovieBuilderComparer implements Comparator<Movie> {

	@Override
	public int compare(Movie o1, Movie o2) {
		int id1 = o1.getIdentifierRating();
		int id2 = o2.getIdentifierRating();
		
		if (id1 > id2)
			return -1;
		else
			if (id1 == id2)
				return 0;
			else
				return 1;
	}
}
