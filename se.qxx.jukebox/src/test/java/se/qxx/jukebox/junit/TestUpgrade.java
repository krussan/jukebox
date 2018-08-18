package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import fi.iki.elonen.NanoHTTPD.Response;
import se.qxx.jukebox.upgrade.Upgrade_0_20;
import se.qxx.jukebox.webserver.StreamingWebServer;
import se.qxx.protodb.backend.DatabaseBackend;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class TestUpgrade {
	@Test
	public void TestUpgrade_0_20() throws DatabaseNotSupportedException {
		DatabaseBackend db = new MockDatabaseBackend();
		Upgrade_0_20 up = new Upgrade_0_20();
		List<String> dbScripts = up.getDatabaseUpgradeScripts(db);
		
		assertEquals(8, dbScripts.size());
		assertEquals("CREATE TABLE MediaConverterState (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, value TEXT NOT NULL)", dbScripts.get(0));
		assertEquals("INSERT INTO MediaConverterState (value) VALUES ('NotNeeded')", dbScripts.get(1));
		assertEquals("INSERT INTO MediaConverterState (value) VALUES ('Queued')", dbScripts.get(2));
		assertEquals("INSERT INTO MediaConverterState (value) VALUES ('Completed')", dbScripts.get(3));
		assertEquals("INSERT INTO MediaConverterState (value) VALUES ('Converting')", dbScripts.get(4));
		assertEquals("INSERT INTO MediaConverterState (value) VALUES ('Failed')", dbScripts.get(5));
		assertEquals("ALTER TABLE Media ADD COLUMN _downloadcomplete_ID INTEGER NULL REFERENCES MediaConverterState(ID)", dbScripts.get(6));
		assertEquals("UPDATE Media SET _downloadcomplete_ID = 2", dbScripts.get(7));
	}
}
