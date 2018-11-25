package se.qxx.jukebox.tests;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;

public class TestParserBuilder {
	
	private IJukeboxLogger log;
	private ISettings settings;
	
	@Inject
	public TestParserBuilder(ISettings settings, LoggerFactory factory) {
		this.settings = settings;
		this.log = factory.create(LogType.FIND);
		
	}
	
	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			Injector injector = Binder.setupBindings(args);
			TestParserBuilder prog = injector.getInstance(TestParserBuilder.class);
			prog.execute(args[0]);

		}
	}
	
	public void execute(String filename) {
		ParserBuilder b = new ParserBuilder(settings, log);
		ParserMovie pm = b.extractMovieParser("", filename);
			
		System.out.println(pm.toString());
	}
	
}
