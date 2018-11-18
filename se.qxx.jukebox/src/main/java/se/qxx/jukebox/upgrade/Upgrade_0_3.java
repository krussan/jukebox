package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_3 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_3(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"ALTER TABLE Movie ADD identifier varchar(20)"
	  , "ALTER TABLE Movie ADD identifierRating int"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,3);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,2);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
