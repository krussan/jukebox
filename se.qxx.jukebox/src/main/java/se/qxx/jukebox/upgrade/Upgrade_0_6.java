package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_6 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_6(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"ALTER TABLE Movie ADD watched bit NOT NULL default (0)"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,6);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,5);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
