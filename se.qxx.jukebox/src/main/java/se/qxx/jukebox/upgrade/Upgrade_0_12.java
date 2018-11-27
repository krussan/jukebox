package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IUpgrader;

public class Upgrade_0_12 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_12(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"INSERT INTO identifier (ID, value) VALUES (4, 'Parser')"
		
//		"CREATE TABLE tv_seasons (" +
//		"   ID int NOT NULL PRIMARY KEY," +
//		" , 
		
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,12);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,11);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
