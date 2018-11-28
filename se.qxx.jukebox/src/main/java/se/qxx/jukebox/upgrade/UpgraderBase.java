package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IUpgrader;

public class UpgraderBase {

	private IDatabase database;
	
	public UpgraderBase(IDatabase database) {
		this.setDatabase(database);
	}
	
	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	public void runDatabasescripts(String[] dbScripts) throws UpgradeFailedException {
		System.out.println("Upgrading database...");

		if (!this.getDatabase().executeUpgradeStatements(dbScripts))
			throw new UpgradeFailedException();
	}

}
