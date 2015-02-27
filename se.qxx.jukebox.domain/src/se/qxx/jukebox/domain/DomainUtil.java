package se.qxx.jukebox.domain;

import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class DomainUtil {
	public static Series updateSeason(Series series, Season newSeason) {
		int seasonIndex = findSeasonIndex(series, newSeason.getSeasonNumber());
		Series.Builder sb = Series.newBuilder(series);
		
		if (seasonIndex>=0)
			sb.removeSeason(seasonIndex);
		
		
		return sb.addSeason(newSeason).build();
	}
	
	public static Season updateEpisode(Season season, Episode newEpisode) {
		int episodeIndex = findEpisodeIndex(season, newEpisode.getEpisodeNumber());
		Season.Builder sb = Season.newBuilder(season);
		
		if (episodeIndex >= 0)
			sb.removeEpisode(episodeIndex);
		
		return sb.addEpisode(newEpisode).build();
	}
	
	public static int findSeasonIndex(Series s, int season) {
		for (int i=0; i<s.getSeasonCount(); i++){
			if (s.getSeason(i).getSeasonNumber() == season)
				return i;
		}
		
		return -1;
	}
	
	public static int findEpisodeIndex(Season sn, int episode) {
		for (int i=0; i<sn.getEpisodeCount(); i++){
			if (sn.getEpisode(i).getEpisodeNumber() == episode)
				return i;
		}
		
		return -1;
	}
}
