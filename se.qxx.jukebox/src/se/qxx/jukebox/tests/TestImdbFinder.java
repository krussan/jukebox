package se.qxx.jukebox.tests;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;  

import se.qxx.jukebox.IMDBFinder;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;

public class TestImdbFinder {   
	public static void main(String[] args) throws Exception {
		Settings.initialize();
 
		Log.Debug(String.format("Number of infopatterns :: %s", Settings.imdb().getInfoPatterns().getInfoPattern().size()), LogType.IMDB);
		if (args.length > 0 && !StringUtils.isEmpty(args[0])) {
			String filename = args[0];
			String path = FilenameUtils.getPath(filename);
			filename = FilenameUtils.getName(filename);
			
			MovieOrSeries mos = MovieBuilder.identify(path, filename);
			
			System.out.println(String.format("Title :: %s", mos.getTitle()));
			
			if (!mos.isSeries()){
				Movie mm = IMDBFinder.Get(mos.getMovie());
				System.out.println(mm);
			}
			else {
				Series ss = mos.getSeries();
				ss = IMDBFinder.Get(ss, 
						ss.getSeason(0).getSeasonNumber(),
						ss.getSeason(0).getEpisode(0).getEpisodeNumber());
				
				System.out.println(ss);
			}
			
		}
	}
}
