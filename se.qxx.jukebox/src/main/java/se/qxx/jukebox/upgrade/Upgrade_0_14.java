package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IUpgrader;

public class Upgrade_0_14 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_14(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"ALTER TABLE movie ADD identifiedTitle TEXT NULL",
		"UPDATE movie SET identifiedTitle = title"
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,14);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,13);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);
		
	}
}
