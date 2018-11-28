package se.qxx.jukebox.upgrade;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.backend.DatabaseBackend;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.FieldNotFoundException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public class Upgrade_0_31 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_31(IDatabase database) {
		super(database);
	}

	@Override
	public Version getThisVersion() {
		return new Version(0,31);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,20);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		try {
			ProtoDB db = this.getDatabase().getProtoDBInstance();

			executeScripts(
				getDatabaseUpgradeScripts(
					db.getDatabaseBackend()));
							
		} catch (DatabaseNotSupportedException e) {
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
		

		
	}

	public List<String> getDatabaseUpgradeScripts(DatabaseBackend db) throws DatabaseNotSupportedException {
		
		List<String> updateScripts = new ArrayList<String>();
		
		updateScripts.add(
				String.format("INSERT INTO %1$sMediaConverterState%2$s (%1$sID%2$s, %1$svalue%2$s) VALUES (6, 'Forced')",
						db.getStartBracket(),
						db.getEndBracket()));

		return updateScripts;
	}

	public void executeScripts(List<String> updateScripts) throws UpgradeFailedException {
		runDatabasescripts((String[])updateScripts.toArray(new String[0]));
	}

}
