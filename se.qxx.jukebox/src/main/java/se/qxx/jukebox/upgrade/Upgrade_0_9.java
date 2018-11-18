package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_9 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_9(IDatabase database) {
		super(database);
	}

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
		runDatabasescripts(DbScripts);

	}
}
