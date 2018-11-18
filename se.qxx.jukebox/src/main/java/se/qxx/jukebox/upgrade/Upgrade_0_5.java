package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_5 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_5(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"ALTER TABLE Movie ADD metaDuration int",
		"ALTER TABLE Movie ADD metaFramerate varchar(15)"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,5);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,4);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
