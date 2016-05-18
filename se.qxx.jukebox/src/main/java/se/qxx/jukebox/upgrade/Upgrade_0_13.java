package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_13 implements IIncrimentalUpgrade {

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
		Upgrader.runDatabasescripts(DbScripts);

	}
}
