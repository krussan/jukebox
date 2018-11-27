package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_2 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_2(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"ALTER TABLE Movie ADD duration int"
	  , "ALTER TABLE Movie ADD rating varchar(5)"
	  , "ALTER TABLE Movie ADD director varchar(100)"
	  , "ALTER TABLE Movie ADD story varchar(1024)"
	  , "CREATE TABLE Genre (ID INTEGER PRIMARY KEY, genreName varchar(50) NOT NULL)"
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
		runDatabasescripts(DbScripts);

	}
}
