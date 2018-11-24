package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.mockito.Mock;

import com.google.inject.Injector;

import se.qxx.jukebox.Binder;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.NFOLine;
import se.qxx.jukebox.builders.NFOScanner;
import se.qxx.jukebox.builders.NfoBuilder;
import se.qxx.jukebox.builders.exceptions.SeriesNotSupportedException;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.NFOScannerFactory;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.settings.Settings;

public class TestNfoScanner {
	
	public static void main(String[] args) throws IOException, JAXBException, SeriesNotSupportedException {
		Injector injector = Binder.setupBindings(args);
		NFOScannerFactory nfoScannerFactory = injector.getInstance(NFOScannerFactory.class);
		ISettings settings = injector.getInstance(ISettings.class);
		LoggerFactory loggerFactory = injector.getInstance(LoggerFactory.class);
		IJukeboxLogger log = loggerFactory.create(LogType.FIND);
		
	    String FileName = "example.nfo";
	    System.out.println(String.format("Nr of args\t\t::%s", args.length));
	    if (args.length > 0)
	        FileName = args[0];

	    NFOScanner scanner = nfoScannerFactory.create(new File(FileName));
	    List<NFOLine> lines = scanner.scan();
	    
	    for (int i = 0;i<lines.size();i++) {
	    	System.out.println(String.format("%s : %s\t%s\t\t%s", i, lines.get(i).getType().toString(), lines.get(i).getValue(), lines.get(i).getLine()));
	    }
	    
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("--------------------------------------------------------------");

		String filePath = FilenameUtils.getFullPath(FileName);
		String singleFile = FilenameUtils.getName(FileName);
 
		NfoBuilder builder = new NfoBuilder(settings, log);
		
	    MovieOrSeries mos = builder.extract(filePath, singleFile);
	    Movie m = mos.getMovie();
	    
	    System.out.println(String.format("TITLE\t::\t%s", m.getTitle()));
	    System.out.println(String.format("YEAR\t::\t%s", m.getYear()));
	    System.out.println(String.format("TYPE\t::\t%s", m.getType()));
	    System.out.println(String.format("SOUND\t::\t%s", m.getSound()));
	    System.out.println(String.format("LANGUAGE\t::\t%s", m.getLanguage()));
	    System.out.println(String.format("ID-RATING\t::\t%s", m.getIdentifierRating()));
	    System.out.println(String.format("IMDB-URL\t::\t%s", m.getImdbUrl()));
	    	    
	}
}
