package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.settings.FindersTest;
import se.qxx.jukebox.subtitles.Language;
import se.qxx.jukebox.subtitles.SubscenePost;

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

	public static void main(String[] args) {
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
		settings.getSettings().getSubfinders().setPath(subsPath);
		
		try {
			for (FindersTest f : settings.getSettings().getSubfinders().getFinders()) {
				if (f.getExecutor().endsWith("SubscenePost")) {
					
					SubscenePost s = new SubscenePost(subFileDownloader, f);
					
					File ff = new File(filename);

					MovieOrSeries mos = movieBuilderFactory.identify(ff.getAbsolutePath(), ff.getName());
					ArrayList<Language> languages = new ArrayList<Language>();
					languages.add(Language.English);
					languages.add(Language.Swedish);

					s.findSubtitles(mos, languages);

				}
			}
			
			
			
		} catch (Exception e) {
			System.out.println("failed to get subtitles");
			System.out.println(e.toString());

		}
	}

}
