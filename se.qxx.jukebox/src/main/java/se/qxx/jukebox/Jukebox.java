package se.qxx.jukebox;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.upgrade.Upgrader;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public class Jukebox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Arguments.initialize(args);
		 
		
		if (Arguments.get().isHelpRequested()) {
			displayHelp(); 
			return; 
		}
		
		try {
			Settings.initialize();
		
			if (Arguments.get().isPurgeMode()) {
				purge();
				return;
			}
			
			if (Arguments.get().isPurgeSubtitles()) {
	//			purgeSubs();
				System.out.println("Purging of subtitles has been removed. Will maybe be implemented in the future...");
				System.out.println("Exiting....");			
				return;
			}
			
			if (Arguments.get().isPurgeSeries()) {
				System.out.println("Purging all series");
				DB.purgeSeries();
				return;
			}
			
			if (Arguments.get().isSetupDatabase()) {
				try {
					System.out.println("Setting up database...");
				
					DB.setupDatabase();
				} catch (ClassNotFoundException | SQLException | IDFieldNotFoundException
						| DatabaseNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Done!");
				return;
			}
						
			startMainThread();
		} catch (IOException | JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	private static void displayHelp() {
		System.out.println("");
		System.out.println("Jukebox starter - run.sh");
		System.out.println("");
		System.out.println("   run.sh [-ds] [-di] [-dt] [-dm] [-dc] [--purge] [--help]");
		System.out.println("");
		System.out.println("\t-ds\tDisable subtitle downloader");
		System.out.println("\t-di\tDisable imdb identifier");
		System.out.println("\t-dt\tDisable tcp listener");
		System.out.println("\t-dm\tDisable media info library");
		System.out.println("\t-dw\tDisable streaming web server");
		System.out.println("\t-df\tDisable search engine finder");
		System.out.println("\t-dc\tDisable cleaning thread");
		System.out.println("\t-dcl\tDisable but log cleaning entries");
		System.out.println("");
		System.out.println("\t--purge\tPurges all content from database and exit");
		System.out.println("\t--purgeSubs\tPurges all subtitles and queue from database");
		System.out.println("\t--purgeSeries\tPurges all series and tv episodes from database");		
		System.out.println("\t--help\tDisplays this help");
		System.out.println("");
	}
	
	private static void purge() {
		System.out.println("Purging database ....");
		DB.purgeDatabase();
		System.out.println("Done !");
	}

//	private static void purgeSubs() {
//		System.out.println("Purging subtitles from database ....");
//		DB.purgeSubs();
//		System.out.println("Done !");
//	}

	private static void startMainThread()  {
		try {
			if (DB.setupDatabase()) {
				if (!Upgrader.upgradeRequired()) {
					System.out.println("No upgrade required... continuing...");
					Thread t = new Thread(new Main());
					t.start();
					
					t.join();
				}
				else if (Upgrader.databaseIsLaterVersion()) {
					System.out.println("Database is a later version!! Exiting .......");
				}
				else {
					System.out.println("Upgrade required");
					Upgrader.performUpgrade();
				}
			}
		}
		catch (Exception e) {
			//log exception and exit
			System.out.println("Error when starting up ::");
			e.printStackTrace();
		}
	}
	


}
