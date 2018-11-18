package se.qxx.jukebox;

import java.util.Arrays;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.ICleaner;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IMain;
import se.qxx.jukebox.interfaces.IStarter;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.IUpgrader;
import se.qxx.jukebox.upgrade.Upgrader;

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
				bind(IDatabase.class).to(DB.class);
				bind(IUpgrader.class).to(Upgrader.class);
				bind(IStarter.class).to(Starter.class);
				bind(IExecutor.class).to(Executor.class);
				bind(IMain.class).to(Main.class);
				bind(ISubtitleDownloader.class).to(SubtitleDownloader.class);
				bind(ICleaner.class).to(Cleaner.class);
				
					
				
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
