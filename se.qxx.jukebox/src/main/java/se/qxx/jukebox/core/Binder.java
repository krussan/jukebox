package se.qxx.jukebox.core;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.concurrent.Executor;
import se.qxx.jukebox.concurrent.JukeboxPriorityQueue;
import se.qxx.jukebox.concurrent.JukeboxThreadPoolExecutor;
import se.qxx.jukebox.converter.ConvertedFile;
import se.qxx.jukebox.converter.MediaConverter;
import se.qxx.jukebox.factories.*;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.imdb.IMDBParser;
import se.qxx.jukebox.imdb.IMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IConvertedFile;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IDownloadChecker;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFileCreatedHandler;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IFileSystemWatcher;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IJukeboxRpcServerConnection;
import se.qxx.jukebox.interfaces.IMain;
import se.qxx.jukebox.interfaces.IMediaConverter;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;
import se.qxx.jukebox.interfaces.IMkvSubtitleReader;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.IRandomWaiter;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStarter;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.interfaces.ISubFileUtilHelper;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.jukebox.interfaces.ITcpListener;
import se.qxx.jukebox.interfaces.IUnpacker;
import se.qxx.jukebox.interfaces.IUpgrader;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.interfaces.IVLCConnection;
import se.qxx.jukebox.interfaces.IWakeOnLan;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.servercomm.JukeboxRpcServerConnection;
import se.qxx.jukebox.servercomm.TcpListener;
import se.qxx.jukebox.servercomm.WakeOnLan;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.subtitles.MkvSubtitleReader;
import se.qxx.jukebox.subtitles.SubFileDownloaderHelper;
import se.qxx.jukebox.subtitles.SubFileUtilHelper;
import se.qxx.jukebox.subtitles.SubtitleDownloader;
import se.qxx.jukebox.subtitles.SubtitleFileWriter;
import se.qxx.jukebox.tools.MediaMetadataHelper;
import se.qxx.jukebox.tools.Unpacker;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebRetriever;
import se.qxx.jukebox.upgrade.Upgrader;
import se.qxx.jukebox.vlc.Distributor;
import se.qxx.jukebox.vlc.VLCConnection;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.FileCreatedHandler;
import se.qxx.jukebox.watcher.FileSystemWatcher;
import se.qxx.jukebox.watcher.FilenameChecker;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class Binder {
	public static Injector setupBindings(String[] args) {
		
		Injector injector = Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(new TypeLiteral<List<String>>() {})
					.annotatedWith(Names.named("Commandline arguments"))
					.toInstance(Arrays.asList(args));
				
				//Settings
				bind(ISettings.class).to(Settings.class);
				bind(IImdbSettings.class).to(ImdbSettings.class);
				bind(IParserSettings.class).to(ParserSettings.class);
				
				//Core
				bind(IArguments.class).to(Arguments.class).asEagerSingleton();
				bind(IExecutor.class).to(Executor.class);
				bind(IMain.class).to(Main.class);
				bind(IDatabase.class).to(DB.class);
				bind(IUpgrader.class).to(Upgrader.class);
				bind(IStarter.class).to(Starter.class);
				bind(IRandomWaiter.class).to(RandomWaiter.class);
				bind(IUtils.class).to(Util.class);
				bind(ExecutorService.class).to(JukeboxThreadPoolExecutor.class);
				bind(JukeboxPriorityQueue.class);
				
				//Webserver
				install(
					new FactoryModuleBuilder()
						.implement(IStreamingWebServer.class, StreamingWebServer.class)
						.build(WebServerFactory.class));
				
				//Watcher
				bind(ICleaner.class).to(Cleaner.class);
				bind(IDownloadChecker.class).to(DownloadChecker.class);
				bind(IFileCreatedHandler.class).to(FileCreatedHandler.class);
				bind(IFileReader.class).to(FileReader.class);
				bind(IMediaMetadataHelper.class).to(MediaMetadataHelper.class);
				bind(IMovieBuilderFactory.class).to(MovieBuilderFactory.class);
				bind(IMovieIdentifier.class).to(MovieIdentifier.class);
				bind(IFilenameChecker.class).to(FilenameChecker.class);

				install(
					new FactoryModuleBuilder()
						.implement(IFileSystemWatcher.class, FileSystemWatcher.class)
						.build(FileSystemWatcherFactory.class));

				//IMDB
				bind(IIMDBFinder.class).to(IMDBFinder.class);
				bind(IIMDBUrlRewrite.class).to(IMDBUrlRewrite.class);
				
				install(
					new FactoryModuleBuilder()
						.implement(IIMDBParser.class, IMDBParser.class)
						.build(IMDBParserFactory.class));

				
				//Tcp Listener
				install(
					new FactoryModuleBuilder()
						.implement(ITcpListener.class, TcpListener.class)
						.build(TcpListenerFactory.class));
				
				install(
					new FactoryModuleBuilder()
						.implement(IJukeboxRpcServerConnection.class, JukeboxRpcServerConnection.class)
						.build(JukeboxRpcServerFactory.class));

				//ExecutorService
				install(
					new FactoryModuleBuilder()
						.implement(ExecutorService.class, JukeboxThreadPoolExecutor.class)
						.build(ExecutorServiceFactory.class));

				//Converter
				bind(IMediaConverter.class).to(MediaConverter.class);
				
				//Comm
				bind(IDistributor.class).to(Distributor.class);
				bind(IWebRetriever.class).to(WebRetriever.class);
				bind(IWakeOnLan.class).to(WakeOnLan.class);
				
				//Subs
				bind(ISubtitleDownloader.class).to(SubtitleDownloader.class);
				bind(ISubFileDownloaderHelper.class).to(SubFileDownloaderHelper.class);
				bind(ISubtitleFileWriter.class).to(SubtitleFileWriter.class);
				bind(IMkvSubtitleReader.class).to(MkvSubtitleReader.class);
				bind(ISubFileUtilHelper.class).to(SubFileUtilHelper.class);
				
				//Unpacker
				bind(IUnpacker.class).to(Unpacker.class);
			
				//Log
				install(
						new FactoryModuleBuilder()
							.implement(IJukeboxLogger.class, Log.class)
							.build(LoggerFactory.class));
				
				//VLCConnection
				install(
					new FactoryModuleBuilder()
						.implement(IVLCConnection.class, VLCConnection.class)
						.build(VLCConnectionFactory.class));

				//ConvertedFile
				install(
					new FactoryModuleBuilder()
						.implement(IConvertedFile.class, ConvertedFile.class)
						.build(ConvertedFileFactory.class));
					
			}
		});
		
		return injector;
	}

}
