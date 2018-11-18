package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IUpgrader;

public class Upgrade_0_1 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_1(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"CREATE TABLE dbVersion (major int, minor int)"
	  , "INSERT INTO dbVersion (major, minor) VALUES (0,1)"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,1);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,0);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		// this is the first upgrade. 
		// really no need to perform the upgrade. We will just list all the create statements here:
		
		/*
			CREATE TABLE [Movie] (
				[ID] integer  PRIMARY KEY NOT NULL,
				[filename] varchar(255)  NULL,
				[filepath] varchar(255)  NULL,
				[title] varchar(255)  NULL,
				[year] integer  NULL,
				[type] varchar(50)  NULL,
				[format] varchar(50)  NULL,
				[sound] varchar(50)  NULL,
				[language] varchar(50)  NULL,
				[groupName] varchar(50)  NULL,
				[imdburl] varchar(255)  NULL
			)
			
			CREATE TABLE subtitleQueue (
				  _movie_ID int NOT NULL REFERENCES Movie(ID)
				, queuedAt datetime
				, retreivedAt datetime
				, result int
			)
			
			CREATE TABLE subtitles (
				_movie_ID int NOT NULL REFERENCES Movie(ID),
				filename varchar(255),
				description varchar(255),
				rating varchar(30)
			)			
			
		 * 
		 */

		runDatabasescripts(DbScripts);
		
	}

}
