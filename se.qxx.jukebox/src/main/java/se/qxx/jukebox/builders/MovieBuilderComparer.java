package se.qxx.jukebox.builders;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.MovieOrSeries;

public class MovieBuilderComparer implements Comparator<MovieOrSeries> {

	@Override
	public int compare(MovieOrSeries o1, MovieOrSeries o2) {
		int id1 = getIdentifierRating(o1);
		int id2 = getIdentifierRating(o2);
	
		// favor the one that has an identified imdb url over the other
		int result = compareImdbUrl(o1, o2);
		if (result != 0) {				
			if (id1 > id2)
				result = -1;
			else
				if (id1 == id2)
					result = 0;
				else
					result = 1;
		}
		
		return result;
	}


	private int compareImdbUrl(MovieOrSeries o1, MovieOrSeries o2) {
		if (!StringUtils.isEmpty(getImdbUrl(o1)))
			return -1;
		
		if (!StringUtils.isEmpty(getImdbUrl(o2)))
			return 1;

		return 0;
	}

	private int getIdentifierRating(MovieOrSeries o1) {
		if (o1.isSeries()) {
			return o1.getSeries().getSeason(0).getEpisode(0).getIdentifierRating();
		}
		else {
			return o1.getMovie().getIdentifierRating();
		}
	}
	
	private String getImdbUrl(MovieOrSeries mos) {
		if (mos.isSeries()) {
			return mos.getSeries().getSeason(0).getEpisode(0).getImdbUrl();
		} else {
			return mos.getMovie().getImdbUrl();
		}
	}

}
