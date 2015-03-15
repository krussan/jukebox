package se.qxx.jukebox.builders;

import java.util.Comparator;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class MovieBuilderComparer implements Comparator<MovieOrSeries> {

	@Override
	public int compare(MovieOrSeries o1, MovieOrSeries o2) {
		int id1 = getIdentifierRating(o1);
		int id2 = getIdentifierRating(o2);
		
		if (id1 > id2)
			return -1;
		else
			if (id1 == id2)
				return 0;
			else
				return 1;
	}

	private int getIdentifierRating(MovieOrSeries o1) {
		if (o1.isSeries()) {
			return o1.getSeries().getSeason(0).getEpisode(0).getIdentifierRating();
		}
		else {
			return o1.getMovie().getIdentifierRating();
		}
	}
}
