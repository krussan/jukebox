package se.qxx.jukebox.tests;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;  

import se.qxx.jukebox.IMDBFinder;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.builders.PartPattern;
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
			
			PartPattern pp = new PartPattern(filename);

			Movie m = MovieBuilder.identifyMovie(path, filename);
			Series s = MovieBuilder.identifySeries(m, path, filename);
			
			if (s == null){
				Movie mm = IMDBFinder.Get(m);
				System.out.println(mm);
			}
			else {
				Series ss = IMDBFinder.Get(s, pp.getSeason(), pp.getEpisode(), true, true, true);
				System.out.println(ss);
			}
			
		}
	}
}
