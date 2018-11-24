package se.qxx.jukebox.tests;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;

public class TestMovieBuilder {

	private IMovieBuilderFactory movieBuilderFactory;

	@Inject
	public TestMovieBuilder(IMovieBuilderFactory movieBuilderFactory) {
		this.movieBuilderFactory = movieBuilderFactory;
		
	}
	
	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			Injector injector = Binder.setupBindings(args);
			TestMovieBuilder prog = injector.getInstance(TestMovieBuilder.class);
			
			prog.execute(args[0]);
		}
		else {
			System.out.println("No arguments");
		}
	}
	
	public void execute(String filename) {
		String filePath = FilenameUtils.getFullPathNoEndSeparator(filename);
		String singleFile = FilenameUtils.getName(filename);
		
		System.out.println(filePath);
		System.out.println(singleFile);
		
		MovieOrSeries mos = movieBuilderFactory.identify(filePath, singleFile);

		System.out.println(String.format("Movie identified by :: %s", mos.getIdentifier()));
		
		if (mos.isSeries())
			System.out.println(mos.getSeries().toString());
		else
			System.out.println(mos.getMovie().toString());
		
	}
}
