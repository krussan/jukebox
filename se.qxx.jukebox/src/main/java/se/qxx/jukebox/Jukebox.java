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
import se.qxx.jukebox.concurrent.Executor;
import se.qxx.jukebox.converter.MediaConverter;
import se.qxx.jukebox.core.Arguments;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Cleaner;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.FileReader;
import se.qxx.jukebox.core.Main;
import se.qxx.jukebox.core.MovieIdentifier;
import se.qxx.jukebox.core.Starter;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.factories.FileSystemWatcherFactory;
import se.qxx.jukebox.factories.IMDBParserFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.imdb.IMDBParser;
import se.qxx.jukebox.imdb.IMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IFileSystemWatcher;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMain;
import se.qxx.jukebox.interfaces.IMediaConverter;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStarter;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.jukebox.interfaces.ITcpListener;
import se.qxx.jukebox.interfaces.IUnpacker;
import se.qxx.jukebox.interfaces.IUpgrader;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.servercomm.TcpListener;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.subtitles.IMkvSubtitleReader;
import se.qxx.jukebox.subtitles.MkvSubtitleReader;
import se.qxx.jukebox.subtitles.SubFileDownloaderHelper;
import se.qxx.jukebox.subtitles.SubtitleDownloader;
import se.qxx.jukebox.subtitles.SubtitleFileWriter;
import se.qxx.jukebox.tools.Unpacker;
import se.qxx.jukebox.tools.WebRetriever;
import se.qxx.jukebox.upgrade.Upgrader;
import se.qxx.jukebox.vlc.Distributor;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.FileCreatedHandler;
import se.qxx.jukebox.watcher.FileSystemWatcher;
import se.qxx.jukebox.watcher.IDownloadChecker;
import se.qxx.jukebox.watcher.IFileCreatedHandler;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class Jukebox {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Injector injector = Binder.setupBindings(args);
		Starter starter = injector.getInstance(Starter.class);
		Main main = injector.getInstance(Main.class);
		
		if (starter.checkStart())
			main.run();
	}


}
