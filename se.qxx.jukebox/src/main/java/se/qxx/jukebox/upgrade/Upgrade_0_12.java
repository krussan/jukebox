package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_12 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"INSERT INTO identifier (ID, value) VALUES (4, 'Parser')"
		
//		"CREATE TABLE tv_seasons (" +
//		"   ID int NOT NULL PRIMARY KEY," +
//		" , 
		
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,12);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,11);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

	}
}
