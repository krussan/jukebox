package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_11 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_11(IDatabase database) {
		super(database);
	}

	
	static final String[] DbScripts = {
		"CREATE TABLE Movie_Blacklist (_movie_ID INTEGER NOT NULL REFERENCES Movie (ID),value TEXT NOT NULL)",
		"DROP TABLE Media_Blacklist",
		"CREATE TABLE Version (ID INTEGER PRIMARY KEY AUTOINCREMENT, [major] INTEGER NOT NULL,[minor] INTEGER NOT NULL)",
		"INSERT INTO Version (major, minor) VALUES (0,11)"
				
//		"CREATE TABLE tv_seasons (" +
//		"   ID int NOT NULL PRIMARY KEY," +
//		" , 
		
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,11);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,10);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
