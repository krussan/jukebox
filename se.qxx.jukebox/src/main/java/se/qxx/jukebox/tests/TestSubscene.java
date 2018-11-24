package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.factories.NFOScannerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.subtitles.Subscene;

public class TestSubscene {
	
	private ISettings settings;
	private MovieBuilderFactory movieBuilderFactory;
	private ISubFileDownloaderHelper subFileDownloader;

	@Inject
	public TestSubscene(ISettings settings, 
			MovieBuilderFactory movieBuilderFactory,
			ISubFileDownloaderHelper subFileDownloader) {
		
		this.settings = settings;
		this.movieBuilderFactory = movieBuilderFactory;
		this.subFileDownloader = subFileDownloader;
		
	}

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 1) {
			Injector injector = Binder.setupBindings(args);
			TestSubscene prog = injector.getInstance(TestSubscene.class);
			prog.execute(args[0], args[1]);
						
		}
		else {
			System.out.println("No arguments");
		}
	}

	public void execute(String filename, String subsPath) {
		settings.getSettings().getSubFinders().setSubsPath(subsPath);
		
		try {
			for (SubFinder f : settings.getSettings().getSubFinders().getSubFinder()) {
				if (f.getClazz().endsWith("Subscene")) {
					
					Subscene s = new Subscene(subFileDownloader);
					
					File ff = new File(filename);
					
					if (ff.exists()) {
						
						
						MovieOrSeries mos = movieBuilderFactory.identify(ff.getAbsolutePath(), ff.getName());
						ArrayList<String> languages = new ArrayList<String>();
						languages.add("Eng");
						languages.add("Swe");
						
						s.findSubtitles(mos, languages);
						
					}
					
				}
			}
			
			
			
		} catch (Exception e) {
			System.out.println("failed to get subtitles");
			System.out.println(e.toString());

		}
	}

}
