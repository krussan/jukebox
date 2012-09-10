package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.util.List;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.IMDBFinder;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class Upgrade_0_3 implements IIncrimentalUpgrade {

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
		Upgrader.runDatabasescripts(DbScripts);

	}
}
