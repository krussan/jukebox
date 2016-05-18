package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_8 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"CREATE TABLE Language (" +
		"   ID integer PRIMARY KEY NOT NULL" +
		" , language varchar(50) NOT NULL" +
		" , shortname varchar(5) NOT NULL" +
		")",
		
		"INSERT INTO Language (language, shortname) VALUES ('Unknown', '---')",
		"INSERT INTO Language (language, shortname) VALUES ('Swedish', 'SWE')",
		"INSERT INTO Language (language, shortname) VALUES ('English', 'ENG')",
		
		
		
		"ALTER TABLE subtitles ADD _subtitleLanguage_ID int NULL REFERENCES Language(ID)"
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,8);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,7);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

	}
}
