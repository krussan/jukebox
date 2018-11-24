package se.qxx.jukebox.tests;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Injector;

import se.qxx.jukebox.Binder;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.settings.Settings;

public class TestImdbFinder {   
	public static void main(String[] args) throws Exception {
		Injector injector = Binder.setupBindings(args);
		IIMDBFinder imdbFinder = injector.getInstance(IIMDBFinder.class);
		IMovieBuilderFactory movieBuilderFactory = injector.getInstance(IMovieBuilderFactory.class);
		
		if (args.length > 0 && !StringUtils.isEmpty(args[0])) {
			String filename = args[0];
			String path = FilenameUtils.getPath(filename);
			filename = FilenameUtils.getName(filename);
			
			MovieOrSeries mos = movieBuilderFactory.identify(path, filename);
			
			System.out.println(String.format("Title :: %s", mos.getTitle()));
			
			if (!mos.isSeries()){
				Movie mm = imdbFinder.Get(mos.getMovie());
				System.out.println(mm);
			}
			else {
				Series ss = mos.getSeries();
				ss = imdbFinder.Get(ss, 
						ss.getSeason(0).getSeasonNumber(),
						ss.getSeason(0).getEpisode(0).getEpisodeNumber());
				
				System.out.println(ss);
			}
			
		}
	}
}
