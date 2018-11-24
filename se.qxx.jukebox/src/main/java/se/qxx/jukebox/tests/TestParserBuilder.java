package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import com.google.inject.Injector;

import se.qxx.jukebox.Binder;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.NFOScannerFactory;
import se.qxx.jukebox.settings.Settings;

public class TestParserBuilder {
	public static void main(String[] args) throws IOException, JAXBException {
		Injector injector = Binder.setupBindings(args);
		NFOScannerFactory nfoScannerFactory = injector.getInstance(NFOScannerFactory.class);
		ISettings settings = injector.getInstance(ISettings.class);
		LoggerFactory loggerFactory = injector.getInstance(LoggerFactory.class);
		IJukeboxLogger log = loggerFactory.create(LogType.FIND);
		
		if (args.length > 0) {
			ParserBuilder b = new ParserBuilder(settings, log);
			ParserMovie pm = b.extractMovieParser("", args[0]);
				
			System.out.println(pm.toString());
		}
	}
	
}
