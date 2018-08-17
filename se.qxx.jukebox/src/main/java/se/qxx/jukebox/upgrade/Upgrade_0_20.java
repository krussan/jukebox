package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.google.protobuf.Descriptors.EnumValueDescriptor;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.MediaMetadata;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.backend.DatabaseBackend;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class Upgrade_0_20 implements IIncrimentalUpgrade {

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
			DatabaseBackend db = DB.getProtoDBInstance().getDatabaseBackend();
			String tableName = "MediaConverterState";
			
			List<String> updateScripts = new ArrayList<String>();
			updateScripts.add(
					String.format("CREATE TABLE %s%s%s (%s, value TEXT NOT NULL)",
						db.getStartBracket(),
						tableName,
						db.getEndBracket(),
						db.getIdentityDefinition()));
			
			for (EnumValueDescriptor value : MediaConverterState.getDescriptor().getValues()) {
				String sql = "INSERT INTO %s (value) VALUES (%s%s%s)";
				updateScripts.add(
						String.format(
								sql,
								db.getStartBracket(),
								value.getName(),
								db.getEndBracket()));
			}
			
			Upgrader.runDatabasescripts((String[])updateScripts.toArray());
							
		} catch (DatabaseNotSupportedException e) {
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
		

		
	}

}
