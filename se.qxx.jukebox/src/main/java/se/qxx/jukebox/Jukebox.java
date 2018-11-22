package se.qxx.jukebox;

import java.util.Arrays;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.converter.MediaConverter;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.factories.IMDBParserFactory;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.imdb.IMDBParser;
import se.qxx.jukebox.imdb.IMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IMain;
import se.qxx.jukebox.interfaces.IMediaConverter;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStarter;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.IUpgrader;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.servercomm.ITcpListener;
import se.qxx.jukebox.servercomm.TcpListener;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.tools.WebRetriever;
import se.qxx.jukebox.upgrade.Upgrader;
import se.qxx.jukebox.vlc.Distributor;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.IDownloadChecker;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class Jukebox {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Injector injector = setupBindings(args);
		Starter starter = injector.getInstance(Starter.class);
		Main main = injector.getInstance(Main.class);
		
		if (starter.checkStart())
			main.run();
	}
	
	private static Injector setupBindings(String[] args) {
		
		Injector injector = Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(new TypeLiteral<List<String>>() {})
					.annotatedWith(Names.named("Commandline arguments"))
					.toInstance(Arrays.asList(args));
				
				bind(IArguments.class).to(Arguments.class).asEagerSingleton();
				bind(ISettings.class).to(Settings.class);
				bind(IImdbSettings.class).to(ImdbSettings.class);
				bind(IParserSettings.class).to(ParserSettings.class);
				bind(IStreamingWebServer.class).to(StreamingWebServer.class);
				bind(IDatabase.class).to(DB.class);
				bind(IUpgrader.class).to(Upgrader.class);
				bind(IStarter.class).to(Starter.class);
				bind(IExecutor.class).to(Executor.class);
				bind(IMain.class).to(Main.class);
				bind(ISubtitleDownloader.class).to(SubtitleDownloader.class);
				bind(ICleaner.class).to(Cleaner.class);
				bind(IIMDBFinder.class).to(IMDBFinder.class);
				bind(IMovieIdentifier.class).to(MovieIdentifier.class);
				bind(ITcpListener.class).to(TcpListener.class);
				bind(IDownloadChecker.class).to(DownloadChecker.class);
				bind(IMediaConverter.class).to(MediaConverter.class);
				bind(IDistributor.class).to(Distributor.class);
				bind(IMovieBuilderFactory.class).to(MovieBuilderFactory.class);
				bind(IFileReader.class).to(FileReader.class);
				bind(IWebRetriever.class).to(WebRetriever.class);
				bind(IIMDBUrlRewrite.class).to(IMDBUrlRewrite.class);
				
				install(
					new FactoryModuleBuilder()
						.implement(IIMDBParser.class, IMDBParser.class)
						.build(IMDBParserFactory.class));
				
				
			}
		});
		
		return injector;
	}


//	private static void purgeSubs() {
//		System.out.println("Purging subtitles from database ....");
//		DB.purgeSubs();
//		System.out.println("Done !");
//	}

	


}
