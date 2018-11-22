package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_4 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_4(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"CREATE TABLE BlobData (id INTEGER PRIMARY KEY, data BLOB)"
	  , "CREATE TABLE MovieImage (_movie_id int NOT NULL REFERENCES Movie(ID), _blob_id int NOT NULL REFERENCES BlobData(ID), imageType varchar(50))"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,4);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,3);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		runDatabasescripts(DbScripts);

	}
}
