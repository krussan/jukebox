package se.qxx.jukebox.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import com.google.inject.Inject;

import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStarter;
import se.qxx.jukebox.interfaces.IUpgrader;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public class Starter implements IStarter {
	private IArguments arguments;
	private IUpgrader upgrader;
	private IDatabase database;
	private ISettings settings;
	

	@Inject
	public Starter(IArguments arguments, IUpgrader upgrader, IDatabase database, ISettings settings) {
		this.setArguments(arguments);
		this.setUpgrader(upgrader);
		this.setDatabase(database);
		this.setSettings(settings);
	}
	
	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	public IUpgrader getUpgrader() {
		return upgrader;
	}
	public void setUpgrader(IUpgrader upgrader) {
		this.upgrader = upgrader;
	}
	public IArguments getArguments() {
		return arguments;
	}
	public void setArguments(IArguments arguments) {
		this.arguments = arguments;
	}

	
	private void displayHelp() {
		System.out.println("");
		System.out.println("Jukebox starter - run.sh");
		System.out.println("");
		System.out.println("   run.sh [-ds] [-di] [-dt] [-dm] [-dc] [-dmc] [-dd] [--purge] [--help]");
		System.out.println("");
		System.out.println("\t-ds\tDisable subtitle downloader");
		System.out.println("\t-di\tDisable imdb identifier");
		System.out.println("\t-dt\tDisable tcp listener");
		System.out.println("\t-dm\tDisable media info library");
		System.out.println("\t-dw\tDisable streaming web server");
		System.out.println("\t-df\tDisable search engine finder");
		System.out.println("\t-dc\tDisable cleaning thread");
		System.out.println("\t-dcl\tDisable but log cleaning entries");
		System.out.println("\t-dmc\tDisable media converter");
		System.out.println("\t-dd\tDisable download checker");
		System.out.println("");
		System.out.println("\t--purge\tPurges all content from database and exit");
		System.out.println("\t--purgeSubs\tPurges all subtitles and queue from database");
		System.out.println("\t--purgeSeries\tPurges all series and tv episodes from database");		
		System.out.println("\t--help\tDisplays this help");
		System.out.println("");
	}

	
	public void purge() {
		System.out.println("Purging database ....");
		this.getDatabase().purgeDatabase();
		System.out.println("Done !");
	}
	
	public boolean checkStart() {
		if (this.getArguments().isHelpRequested()) {
			displayHelp(); 
			return false; 
		}
		
		try {
			this.getSettings().initialize();
		
			if (this.getArguments().isPurgeMode()) {
				purge();
				return false;
			}
			
			if (this.getArguments().isPurgeSubtitles()) {
	//			purgeSubs();
				System.out.println("Purging of subtitles has been removed. Will maybe be implemented in the future...");
				System.out.println("Exiting....");			
				return false;
			}
			
			if (this.getArguments().isPurgeSeries()) {
				System.out.println("Purging all series");
				this.getDatabase().purgeSeries();
				return false;
			}
			
			if (this.getArguments().isSetupDatabase()) {
				try {
					System.out.println("Setting up database...");
				
					this.getDatabase().setupDatabase();
				} catch (ClassNotFoundException | SQLException | IDFieldNotFoundException
						| DatabaseNotSupportedException e) {
					e.printStackTrace();
				}
				System.out.println("Done!");
				return false;
			}
						
			return true;
		} catch (IOException | JAXBException e1) {
			e1.printStackTrace();
		}		

		return false;
	}
	
	public boolean checkDatabase() {
		System.out.println("Starting up. Checking database ...");
		
		try {
			if (this.getDatabase().setupDatabase()) {
				if (!this.getUpgrader().upgradeRequired()) {
					System.out.println("No upgrade required... continuing...");
					return true;
				}
				else if (this.getUpgrader().databaseIsLaterVersion()) {
					System.out.println("Database is a later version!! Exiting .......");
					return false;
				}
				else {
					System.out.println("Upgrade required");
					this.getUpgrader().performUpgrade();
					return false;
				}
			}
		}
		catch (ClassNotFoundException | SQLException | IDFieldNotFoundException | DatabaseNotSupportedException | SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			System.out.println("Error occured");
			System.out.println(ex.toString());
			return false;
		}
		
		return false;
	}

}
