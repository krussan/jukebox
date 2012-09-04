package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_2 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"ALTER TABLE Movie ADD duration int"
	  , "ALTER TABLE Movie ADD rating varchar(5)"
	  , "ALTER TABLE Movie ADD director varchar(100)"
	  , "ALTER TABLE Movie ADD story varchar(1024)"
	  , "CREATE TABLE Genre (ID int IDENTITY NOT NULL PRIMARY KEY, genreName varchar(50) NOT NULL)"
	  , "CREATE TABLE MovieGenre (_movie_ID int NOT NULL REFERENCES Movie(ID), _genre_ID int NOT NULL REFERENCES Genre(ID) )"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,2);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,1);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

		//TODO: perform upgrade for version 0.2
		// get duration	
		// update genres
		// get rating
		// get director
		// get story
	}
}
