package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_10 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"CREATE TABLE tv_episodes (" +
		"   _media_ID int NOT NULL REFERENCES media(ID)" +
		" , filename varchar(500) NOT NULL" +
		" , imdbid varchar(50) NOT NULL" +
		" , episode int" +
		" , title varchar(255)" +
		")"		
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
