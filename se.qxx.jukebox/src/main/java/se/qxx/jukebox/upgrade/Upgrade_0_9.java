package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_9 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"CREATE TABLE Blacklist (" +
		"   filepath varchar(500) NOT NULL" +
		" , filename varchar(500) NOT NULL" +
		" , imdbid varchar(50) NOT NULL" +
		")"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,9);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,8);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

	}
}
