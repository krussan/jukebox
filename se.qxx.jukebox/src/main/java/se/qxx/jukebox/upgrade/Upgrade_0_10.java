package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_10 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"ALTER TABLE Movie ADD isTvEpisode bool NOT NULL DEFAULT 0",
		"ALTER TABLE Movie ADD season int NULL",
		"ALTER TABLE Movie ADD episode int NULL",
		"ALTER TABLE Movie ADD firstAirDate date NULL",
		"ALTER TABLE Movie ADD episodeTitle varchar(100) NULL "
				
//		"CREATE TABLE tv_seasons (" +
//		"   ID int NOT NULL PRIMARY KEY," +
//		" , 
		
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,10);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,9);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

	}
}
