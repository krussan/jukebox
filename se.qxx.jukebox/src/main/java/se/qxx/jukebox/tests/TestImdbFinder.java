package se.qxx.jukebox.tests;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.settings.Settings;

public class TestImdbFinder {
	
	private IIMDBFinder imdbFinder;
	private IMovieBuilderFactory movieBuilderFactory;
	
	@Inject
	public TestImdbFinder(IIMDBFinder imdbFinder, IMovieBuilderFactory movieBuilderFactory) {
		this.imdbFinder = imdbFinder;
		this.movieBuilderFactory = movieBuilderFactory;
		
	}
	
	public static void main(String[] args) throws Exception {
		Injector injector = Binder.setupBindings(args);
		TestImdbFinder prog = injector.getInstance(TestImdbFinder.class);
		
		if (args.length > 0 && !StringUtils.isEmpty(args[0])) {
			prog.execute(args[0]);
		}
	}
	
	public void execute(String filename) throws NumberFormatException, IOException, ParseException {
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
