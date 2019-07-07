package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_13 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_13(IDatabase database) {
		super(database);
	}


	static final String[] DbScripts = {
		"ALTER TABLE series ADD identifiedTitle TEXT NULL",
		"UPDATE series SET identifiedTitle = title"
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,13);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,12);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
