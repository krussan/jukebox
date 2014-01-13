package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_10 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"ALTER TABLE movie ADD " + 
	    " isTvEpisode bool NOT NULL DEFAULT 0," +
		" season int NULL, " +
		" episode int NULL, " +
	    " firstAirDate date NULL, " +
		" episodeTitle varchar(100) NULL "
				
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
