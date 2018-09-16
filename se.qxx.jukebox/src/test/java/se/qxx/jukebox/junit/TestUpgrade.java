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
		
		assertEquals(2, dbScripts.size());
		assertEquals(String.format("UPDATE %1$sMedia%2$s SET _converterstate_ID = 2", db.getStartBracket(), db.getEndBracket()), dbScripts.get(0));
		assertEquals(String.format("UPDATE %1$sMedia%2$s SET downloadComplete = 0", db.getStartBracket(), db.getEndBracket()), dbScripts.get(1));
	}
}
