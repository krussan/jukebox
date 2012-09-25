package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.util.List;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.IMDBFinder;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class Upgrade_0_5 implements IIncrimentalUpgrade {

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
		Upgrader.runDatabasescripts(DbScripts);

	}
}
