package se.qxx.jukebox.upgrade;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.backend.DatabaseBackend;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.FieldNotFoundException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public class Upgrade_0_20 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_20(IDatabase database) {
		super(database);
	}

	@Override
	public Version getThisVersion() {
		return new Version(0,20);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,18);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		try {
			ProtoDB db = this.getDatabase().getProtoDBInstance();
			Media md = Media.getDefaultInstance();

			db.addField(md,"converterState");
			db.addField(md, "convertedFileName");
			db.addField(md, "downloadComplete");
			
			executeScripts(
				getDatabaseUpgradeScripts(
					db.getDatabaseBackend()));
							
		} catch (DatabaseNotSupportedException | ClassNotFoundException | IDFieldNotFoundException | SQLException | FieldNotFoundException e) {
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
		

		
	}

	public List<String> getDatabaseUpgradeScripts(DatabaseBackend db) throws DatabaseNotSupportedException {
		
		List<String> updateScripts = new ArrayList<String>();
		
		updateScripts.add(
				String.format("UPDATE %1$sMedia%2$s SET _converterstate_ID = 2",
						db.getStartBracket(),
						db.getEndBracket()));

		updateScripts.add(
				String.format("UPDATE %1$sMedia%2$s SET downloadComplete = 0",
						db.getStartBracket(),
						db.getEndBracket()));

		return updateScripts;
	}

	public void executeScripts(List<String> updateScripts) throws UpgradeFailedException {
		runDatabasescripts((String[])updateScripts.toArray(new String[0]));
	}

}
